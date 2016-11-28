import java.io.*;
import java.net.ProtocolException;
import java.net.Socket;
import java.util.Random;

/**
 * Created by cthill on 11/16/16.
 */
public class TransferClient {
    protected Socket socket;
    protected DataInputStream socketIn;
    protected DataOutputStream socketOut;
    protected Random rand;

    public TransferClient(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.socketIn = new DataInputStream(socket.getInputStream());
        this.socketOut = new DataOutputStream(socket.getOutputStream());
        this.rand = new Random();
    }

    public void transfer(String sourceFilename, String destFilename, boolean asciiArmor, boolean enableXOR, byte[] xorKey, boolean dropRandomPackets, int dropChance) throws IOException {
        // load file
        File file = new File(sourceFilename);
        BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(file));

        // calculate chunks
        long size = file.length();
        int chunks = (int) size / Config.CHUNK_SIZE;
        // chunks are calculated using integer division (which floors), so we might need to add another chunk
        if (size % Config.CHUNK_SIZE != 0) {
            chunks++;
        }

        // send packet header to indicate initiation of file transfer
        socketOut.writeByte(Config.PH_START_TRANSMIT);

        // send filename, file size, chunk size, encoding, etc.
        socketOut.writeUTF(destFilename);
        socketOut.writeLong(size);
        socketOut.writeInt(chunks);
        socketOut.writeBoolean(enableXOR);
        socketOut.writeBoolean(asciiArmor);

        // read each chunk
        for (int i = 0; i < chunks; i++) {
            // calculate number of bytes to read from file
            int readLength = Config.CHUNK_SIZE;
            if (i == chunks - 1) {
                readLength = (int) size % Config.CHUNK_SIZE;
            }

            // allocate buffer for this chunk
            byte[] buffer = new byte[readLength];
            // copy bytes from file into buffer
            fileIn.read(buffer);

            // generate checksum hash
            byte[] checksum = Hash.generateCheckSum(buffer);

            if (enableXOR) {
                buffer = XORCipher.xorCipher(buffer, xorKey);
            }

            int attempts = 0;
            while (true) {
                // send packet header to indicate incoming chunk
                socketOut.writeByte(Config.PH_CHUNK_DATA);

                // send chunk number (i)
                socketOut.writeInt(i);

                if (asciiArmor) {
                    socketOut.writeUTF(Base64.b64Encode(buffer));
                } else {
                    socketOut.writeInt(buffer.length);
                    socketOut.write(buffer);
                }

                // write checksum to socket
                socketOut.writeInt(checksum.length);
                System.out.println(checksum.length);
                if (dropRandomPackets && rand.nextInt(dropChance) == 0) {
                    socketOut.write(new byte[checksum.length]);
                } else {
                    socketOut.write(checksum);
                }
                attempts++;

                // check if chunk was received okay
                byte incoming = socketIn.readByte();
                if (incoming == Config.PH_CHUNK_OK) {
                    break;
                }

                System.out.println("Chunk #" + i + " bad checksum. Retrying (" + attempts + "/" + Config.MAX_CHUNK_RETRY + ")");

                if (attempts >= Config.MAX_CHUNK_RETRY) {
                    System.out.println("Error: exceeded max chunk retries");
                    disconnect();
                    fileIn.close();
                    return;
                }
            }
        }
        fileIn.close();
    }

    /*
        Method to authenticate with server.
        Returns true for successful authentication
     */
    public boolean authenticate(String u, String p) throws IOException {
        // write packet header for auth
        socketOut.writeByte(Config.PH_AUTH);
        // write username
        socketOut.writeUTF(u);
        // write password
        socketOut.writeUTF(p);

        byte responseHeader = socketIn.readByte();
        if (responseHeader == Config.PH_AUTH) {
            // server will return false for bad credentials, true for good
            return (socketIn.readBoolean());
        } else {
            throw new ProtocolException("Expecting auth packet header.");
        }
    }

    public void disconnect() throws IOException {
        socketOut.writeByte(Config.PH_DISCONNECT);
    }

    public void close() {
        try {
            socketOut.close();
            socketIn.close();
            socket.close();
        } catch (IOException e) {
            // who cares?
        }
    }
}