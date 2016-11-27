import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created by cthill on 11/16/16.
 */
public class TransferClient {
    protected Socket socket;
    protected DataInputStream socketIn;
    protected DataOutputStream socketOut;

    public TransferClient(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.socketIn = new DataInputStream(socket.getInputStream()); //reads from server
        this.socketOut = new DataOutputStream(socket.getOutputStream());

//        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//        String fromServer;
//        String fromUser;
//
//        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
//        while ((fromServer = socketIn.readLine()) != null) {
//            System.out.println(fromServer);
//            if (fromServer.equals("You've been authenticated! Good bye!")){
//            	break;
//            }
//            else {
//            	 fromUser = stdIn.readLine();
//
//	            if (fromUser != null) {
//	                out.println(fromUser);
//	            }
//
//            }
//        }
    }

    public void transfer(String sourceFilename, String destFilename) throws FileNotFoundException, IOException {
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
        boolean xor = true;
        boolean asciiArmor = false;

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
                //TODO: read in a key for xor ciphering instead of hard-coding one
                buffer = XORCipher.xorCipher(buffer, "replace this key".getBytes());
            }

            if(asciiArmor == true) {
                buffer = Base64.b64Encode(buffer);
            }

            int attempts = 0;
            while (true) {
                // send packet header to indicate incoming chunk
                socketOut.writeByte(Constants.PH_CHUNK_DATA);
                // send chunk number (i)
                socketOut.writeInt(i);
                // write bytes to socket
                socketOut.writeInt(buffer.length);
                socketOut.write(buffer);
                // write checksum to socket
                socketOut.writeInt(checksum.length);
                socketOut.write(checksum);
                attempts++;

                // check if chunk was reveived okay
                byte incoming = socketIn.readByte();
                if (incoming == Constants.PH_CHUNK_OK) {
                    break;
                } else if (incoming == Constants.PH_PROTO_ERROR) {
                    // print protocol error message
                    System.out.println(socketIn.readUTF());
                    disconnect();
                    return;
                }

                if (attempts >= Constants.CHECK_SUM_REPETITIONS) {
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
            // server will return 0 for bad credentials, 1 for good
            return (socketIn.readByte() != 0);
        } else {
            // TODO: throw exception
            return false;
        }
    }

    public void disconnect() throws IOException {
        socketOut.writeByte(Constants.PH_DISCONNECT);
        socketIn.close();
        socketOut.close();
        socket.close();
    }
}