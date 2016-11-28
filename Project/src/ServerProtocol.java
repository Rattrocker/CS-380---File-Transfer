import java.io.*;
import java.io.IOException;
import java.net.ProtocolException;
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
    protected boolean xor;
    protected boolean asciiArmor;
    FileOutputStream fileOut;
    long fileSize;
    int numChunks;
    int chunkNum;
    int packetsToDrop;

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
            try {
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
            } catch (ProtocolException e) {
                // get error message
                String message = "Protocol error: " + e.getMessage();
                System.out.println(message);
                // write protocol error packet
                socketOut.writeByte(Constants.PH_PROTO_ERROR);
                socketOut.writeUTF(message);
                // disconnect
                socketIn.close();
                socketOut.close();
                done = true;
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

    public void handleStartTransmit() throws IOException, ProtocolException {
        if (receiving) {
            throw new ProtocolException("Received double start_transmit");
        }

        // read file metadata
        String destFilename = socketIn.readUTF();
        fileSize = socketIn.readLong();
        numChunks = socketIn.readInt();
        xor = socketIn.readBoolean();
        asciiArmor = socketIn.readBoolean();

        // create file
        fileOut = new FileOutputStream(destFilename);

        // update state
        receiving = true;
        System.out.println("receiving file: " + destFilename);
    }

    public void handleFileChunk() throws IOException, ProtocolException {
        if (!receiving) {
            throw new ProtocolException("Received unexpected chunk data");
        }

        if (chunkNum >= numChunks) {
            throw new ProtocolException("Received chunkNum exceeding numChunks");
        }

        // read chunk num
        int thisChunkNum = socketIn.readInt();

        // read chunk data
        byte[] chunkData;
        if (asciiArmor) {
            chunkData = Base64.b64Decode(socketIn.readUTF());
        } else {
            int chunkLen = socketIn.readInt();
            chunkData = new byte[chunkLen];
            socketIn.read(chunkData);
        }

        // read checksum data
        int checksumLen = socketIn.readInt();
        byte[] checksumData = new byte[checksumLen];
        socketIn.read(checksumData);
        

        // check chunk number
        if (thisChunkNum != chunkNum) {
            throw new ProtocolException("Expecting chunkNum: " + chunkNum + ", got: " + thisChunkNum);
        }

        // decrypt chunk
        if (xor) {
            chunkData = XORCipher.xorCipher(chunkData, "replace this key".getBytes());
        }
        
        // verify checksum
        byte[] chunkVerify = Hash.generateCheckSum(chunkData);
        if (chunkVerify.length != checksumData.length) {
            socketOut.writeByte(Constants.PH_CHUNK_ERROR);
            return;
        }
        for (int i = 0; i < checksumData.length; i++) {
            if (chunkVerify[i] != checksumData[i]) {
                socketOut.writeByte(Constants.PH_CHUNK_ERROR);
                return;
            }
        }

        // write to file
        fileOut.write(chunkData);
        // update state
        chunkNum++;
        // indicate good chunk
        socketOut.writeByte(Constants.PH_CHUNK_OK);

        // check if we are done
        if (chunkNum == numChunks) {
            fileOut.close();
            receiving = false;
        }
    }
}