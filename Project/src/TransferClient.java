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

    public void transfer(String sourceFilename, String destFilename, byte[] xorKeyFile, boolean enableXOR, boolean dropRandomPackets, int dropChance) throws FileNotFoundException, IOException {
        //get xor key
        byte[] xorKey = new byte[xorKeyFile.length];

        for(int i=0; i < xorKeyFile.length; i++) {
        	xorKey[i] = xorKeyFile[i];
        } 


        // load file
        File file = new File(sourceFilename);
        BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(file));

        // calculate chunks
        long size = file.length();
        int chunks = (int) size / Constants.CHUNK_SIZE;
        // chunks are calculated using integer division (which floors), so we might need to add another chunk
        if (size % Constants.CHUNK_SIZE != 0) {
            chunks++;
        }

        // send packet header to indicate initiation of file transfer
        socketOut.writeByte(Constants.PH_START_TRANSMIT);

        //TODO: parse command line switches for xor and asciiArmor
        boolean xor = enableXOR;
        boolean asciiArmor = true;

        // send filename, file size, chunk size, encoding, etc.
        socketOut.writeUTF(destFilename);
        socketOut.writeLong(size);
        socketOut.writeInt(chunks);
        socketOut.writeBoolean(xor);
        socketOut.writeBoolean(asciiArmor);

        // read each chunk
        for (int i = 0; i < chunks; i++) {
            // calculate number of bytes to read from file
            int readLength = Constants.CHUNK_SIZE;
            if (i == chunks - 1) {
                readLength = (int) size % Constants.CHUNK_SIZE;
            }

            // allocate buffer for this chunk
            byte[] buffer = new byte[readLength];
            // copy bytes from file into buffer
            fileIn.read(buffer);

            // generate checksum hash
            byte[] checksum = Hash.generateCheckSum(buffer);

            if (xor) {
                buffer = XORCipher.xorCipher(buffer, xorKey);
            }

            int attempts = 0;
            while (true) {
                // send packet header to indicate incoming chunk
                socketOut.writeByte(Constants.PH_CHUNK_DATA);

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
                if (dropRandomPackets && rand.nextInt(dropChance) == 0) {
                    socketOut.write(new byte[checksum.length]);
                } else {
                    socketOut.write(checksum);
                }
                attempts++;

                // check if chunk was received okay
                byte incoming = socketIn.readByte();
                if (incoming == Constants.PH_CHUNK_OK) {
                    break;
                }

                System.out.println("Chunk #" + i + " bad checksum. Retrying (" + attempts + "/" + Constants.MAX_CHUNK_RETRY + ")");

                if (attempts >= Constants.MAX_CHUNK_RETRY) {
                    System.out.println("Error: exceeded max chunk retries");
                    disconnect();
                    return;
                }
            }
        }
    }

    /*
        Method to authenticate with server.
        Returns true for successful authentication
     */
    public boolean authenticate(String u, String p) throws IOException {
        // write packet header for auth
        socketOut.writeByte(Constants.PH_AUTH);
        // write username
        socketOut.writeUTF(u);
        // write password
        socketOut.writeUTF(p);

        byte responseHeader = socketIn.readByte();
        if (responseHeader == Constants.PH_AUTH) {
            // server will return false for bad credentials, true for good
            return (socketIn.readBoolean());
        } else {
            throw new ProtocolException("Expecting auth packet header.");
        }
    }

    public void disconnect() throws IOException {
        socketOut.writeByte(Constants.PH_DISCONNECT);
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