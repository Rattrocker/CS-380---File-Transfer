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

        // send filename, file size, chunk size, etc.
        socketOut.writeUTF(destFilename);
        socketOut.writeLong(size);
        socketOut.writeInt(chunks);

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
            //fileIn.read(buffer, 0, readLength);
            fileIn.read(buffer);

            // generate checksum hash
            //byte[] checksum = Hash.generateCheckSum(buffer);

            //TODO: read in a key for xor ciphering instead of hard-coding one
            //buffer = XORCipher.xorCipher(buffer, "replace this key".getBytes());

            //TODO: communicate request for ascii armoring between sender/receiver rahter than hard-coded true
            boolean asciiArmor = false;
            if(asciiArmor == true) {
                buffer = Base64.b64Encode(buffer);
            }

            // send packet header to indicate incoming chunk
            socketOut.writeByte(Constants.PH_CHUNK_DATA);
            // send chunk number (i)
            socketOut.writeInt(i);
            // write bytes to socket
            socketOut.writeInt(buffer.length);
            socketOut.write(buffer);
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
        socket.close();
    }
}