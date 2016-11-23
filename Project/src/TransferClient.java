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
    protected BufferedReader socketIn;
    protected DataOutputStream socketOut;

    public TransferClient(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream())); //reads what client socket is saying
        this.socketOut = new DataOutputStream(socket.getOutputStream());

        String fromServer;
        String fromUser;



        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));            //"stdIn" reads in what the user types.

        while ((fromServer = socketIn.readLine()) != null) {                    //while the server has something to say
            System.out.println("Server: " + fromServer);                        //print out what the server says
            if (fromServer.equals("You've been authenticated! Good bye!"))      //if you have been authenticated, close the connection on client side!
                break;
                    
            fromUser = stdIn.readLine();                                     //read in what the user typed from keyboard
            if (fromUser != null) {
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);      //"out" prints what the user types.
                out.println(fromUser);                                          //echos on the screen what user is typing
            }
        }
    }

    public void transfer(String sourceFilename, String destFilename) throws FileNotFoundException, IOException {
        // load file
        File file = new File(sourceFilename);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

        // calculate chunks
        long size = file.length();
        int chunks = (int) size / Constants.CHUNK_SIZE;
        // chunks are calculated using integer division (which floors), so we might need to add another chunk
        if (size % Constants.CHUNK_SIZE != 0) {
            chunks++;
        }

        //TODO: send packet header to indicate initiation of file transfer
        //TODO: send filename, file size, chunk size, etc.

        // read each chunk
        for (int i = 0; i < chunks; i++) {
            // read <CHUNK_SIZE> bytes into the buffer
            // buffer is padded with 0's if the file size is not divisible by <CHUNK_SIZE>

            // create buffer
            byte[] buffer = new byte[Constants.CHUNK_SIZE];

            // calculate bytes to read from file
            int readLength = Constants.CHUNK_SIZE;
            if (i == chunks - 1) {
                readLength = (int) size % Constants.CHUNK_SIZE;
            }

            // read bytes
            bis.read(buffer, 0, readLength);

            // generate checksum hash
            byte[] checksum = Hash.generateCheckSum(buffer);

            //TODO: read in a key for xor ciphering instead of hard-coding one
            buffer = XORCipher.xorCipher(buffer, "replace this key".getBytes());

            //TODO: communicate request for ascii armoring between sender/receiver rahter than hard-coded true
            boolean asciiArmor = true;
            if(asciiArmor == true) {
                buffer = Base64.b64Encode(buffer);
            }

            //TODO: send packet header to indicate incoming chunk
            //TODO: send chunk number (i)
            //TODO write bytes to socket
        }
    }

    public void disconnect() throws IOException {
        socket.close();
    }
}