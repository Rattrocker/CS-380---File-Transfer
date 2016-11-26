import java.io.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * ServerProtocol and authentication written by Y-Uyen on 11/20/16
 * This class is used to maintain client state and handle packets
 */

public class ServerProtocol {
    protected DataInputStream socketIn;
    protected DataOutputStream socketOut;
    protected HashMap<String, String> loginMap;

    // vars for client state
    protected boolean authenticated = false;
    protected boolean receiving = false;
    FileOutputStream fileOut;
    long fileSize;
    int numChunks;
    int chunkNum;

    public ServerProtocol(DataInputStream in, DataOutputStream out, String loginFilename) {
        socketIn = in;
        socketOut = out;

        // process login file
        loginMap = new HashMap<String,String>();
        try {
            // load login file
            File loginFile = new File(loginFilename);
            Scanner loginScanner = new Scanner(loginFile);

            // parse entries
            while (loginScanner.hasNext()) {
                // read entry from login file
                String line = loginScanner.nextLine();
                String[] split = line.split(":");

                // check formatting
                if (split.length != 2) {
                    System.out.println("Bad login file near: " + line);
                    System.exit(1);
                }

                // add entry to loginMap for lookup later
                loginMap.put(split[0], split[1]);
            }
        } catch (FileNotFoundException e) {
            System.out.println("file not found: " + loginFilename);
            System.exit(1);
        }
    }

    public void run() throws IOException {
        boolean done = false;
        while (!done) {

            // read packet header and run appropriate handler
            byte incoming = socketIn.readByte();
            switch (incoming) {

                case Constants.PH_AUTH:
                    handleAuth();
                    break;

                case Constants.PH_START_TRANSMIT:
                    handleStartTransmit();
                    break;

                case Constants.PH_CHUNK_DATA:
                    handleFileChunk();
                    break;

                case Constants.PH_DISCONNECT:
                    socketIn.close();
                    socketOut.close();
                    done = true;
                    break;
            }
        }
    }

    public void handleAuth() throws IOException {
        // read username and password
        String user = socketIn.readUTF();
        String pass = socketIn.readUTF();

        // TODO: hash password here
        String passhash = pass;

        // check username and password
        boolean goodLogin = false;
        if (loginMap.containsKey(user) && loginMap.get(user).equals(passhash)) {
            goodLogin = true;
        }
        // update client state
        authenticated = goodLogin;

        // write auth header
        socketOut.writeByte(Constants.PH_AUTH);
        // write 1 for successful auth, 0 otherwise
        socketOut.writeByte(goodLogin ? 1 : 0);
    }

    public void handleStartTransmit() throws IOException {
        if (receiving) {
            // TODO: throw an exception and display an error
            return;
        }

        // read file metadata
        String destFilename = socketIn.readUTF();
        fileSize = socketIn.readLong();
        numChunks = socketIn.readInt();

        // create file
        fileOut = new FileOutputStream(destFilename);

        // update state
        receiving = true;
    }

    public void handleFileChunk() throws IOException {
        if (!receiving) {
            // TODO: throw an exception and display an error
            return;
        }

        if (chunkNum >= numChunks) {
            // TODO: throw an exception and display an error
            return;
        }

        // read incoming data
        int chunkNum = socketIn.readInt();
        int chunkLen = socketIn.readInt();
        byte[] chunkData = new byte[chunkLen];
        socketIn.read(chunkData);

        // write to file
        fileOut.write(chunkData);

        if (chunkNum == numChunks - 1) {
            fileOut.close();
            receiving = false;
        }
    }
}