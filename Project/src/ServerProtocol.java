import java.io.*;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * ServerProtocol and authentication written by Y-Uyen on 11/20/16
 * This class is used to maintain client state and handle packets
 */

public class ServerProtocol {
    // objects
    protected Socket clientSocket;
    protected DataInputStream socketIn;
    protected DataOutputStream socketOut;
    protected HashMap<String, String[]> loginMap;
    protected FileOutputStream fileOut;

    // config
    protected boolean xor;
	protected byte[] xorKey;
    protected boolean asciiArmor;

    // client state
    protected boolean done;
    protected boolean authenticated;
    protected boolean receiving;
    int authAttempts;
    long fileSize;
    int numChunks;
    int chunkNum;
    int bytesRead;

    public ServerProtocol(Socket clientSocket, String loginFilename, byte[] xorKey) throws IOException {
        this.clientSocket = clientSocket;
        socketIn = new DataInputStream(clientSocket.getInputStream());
        socketOut = new DataOutputStream(clientSocket.getOutputStream());
        this.xorKey = xorKey;

        // process login file
        loginMap = new HashMap<String,String[]>();
        try {
            // load login file
            File loginFile = new File(loginFilename);
            Scanner loginScanner = new Scanner(loginFile);

            // parse entries
            while (loginScanner.hasNext()) {
                // read entry from login file
                String line = loginScanner.nextLine().trim();
                // skip comments
                if (line.startsWith("#")) {
                    continue;
                }
                // split on colon
                String[] split = line.split(":");

                // check formatting
                if (split.length != 3) {
                    System.out.println("Bad login file near: " + line);
                    System.exit(1);
                }

                // add entry to loginMap for lookup later
                loginMap.put(split[0], new String[] { split[1], split[2] });
            }
        } catch (FileNotFoundException e) {
            System.out.println("file not found: " + loginFilename);
            System.exit(1);
        }
        authAttempts = 0;
    }

    public void run() throws IOException {
        // setup initial state
        done = false;
        authenticated = false;
        receiving = false;

        try {
            while (!done) {
                // read packet header and run appropriate handler
                byte incoming = socketIn.readByte();
                switch (incoming) {
                    case Config.PH_AUTH:
                        handleAuth();
                        break;

                    case Config.PH_START_TRANSMIT:
                        handleStartTransmit();
                        break;

                    case Config.PH_CHUNK_DATA:
                        handleFileChunk();
                        break;

                    case Config.PH_DISCONNECT:
                        disconnect();
                        break;
                }
            }
        }  catch (ProtocolException e) {
            // get error message
            String message = "Protocol error: " + e.getMessage();
            System.out.println(message);
        } catch (EOFException e) {
            // disconnect
        } catch (Exception e) {
            // general exception
            e.printStackTrace();
        } finally {
            socketOut.close();
            socketIn.close();
            clientSocket.close();
            if (receiving) {
                fileOut.close();
                System.out.println("Transfer interrupted.");
            }
        }
    }

    public void disconnect() {
        this.done = true;
    }

    public void handleAuth() throws IOException {
        // read username and password
        String user = socketIn.readUTF();
        String pass = socketIn.readUTF();

        // check if user exists
        if (loginMap.containsKey(user)) {
            // fetch salt and passhash
            String[] data = loginMap.get(user);
            if (data.length == 2) {
                String salt = data[0];
                String passhash = data[1];

                // compare
                try {
                    if (passhash.equals(Hash.generatePasswordHash(salt, pass))) {
                        authenticated = true;
                        System.out.println("Login from: " + user);
                    }
                } catch (NoSuchAlgorithmException e) {
                    System.out.println("Failed to hash password: " + e.getMessage());
                }
            }
        }

        // write auth header
        socketOut.writeByte(Config.PH_AUTH);
        socketOut.writeBoolean(authenticated);

        // check attempts
        if (!authenticated) {
            authAttempts++;
            if (authAttempts >= Config.MAX_AUTH_ATTEMPTS) {
                disconnect();
            }
        }
    }

    public void handleStartTransmit() throws IOException {
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
        bytesRead = 0;
        System.out.println("Receiving file: " + destFilename);
    }

    public void handleFileChunk() throws IOException {
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
            readN(socketIn, chunkData, chunkLen);
        }

        // read checksum data
        int checksumLen = socketIn.readInt();
        byte[] checksumData = new byte[checksumLen];
        readN(socketIn, checksumData, checksumLen);
        

        // check chunk number
        if (thisChunkNum != chunkNum) {
            throw new ProtocolException("Expecting chunkNum: " + chunkNum + ", got: " + thisChunkNum);
        }

        // decrypt chunk
        if (xor) {
            chunkData = XORCipher.xorCipher(chunkData, xorKey);
        }
        
        // verify checksum
        byte[] chunkVerify = Hash.generateCheckSum(chunkData);
        if (chunkVerify.length != checksumData.length) {
            socketOut.writeByte(Config.PH_CHUNK_ERROR);
            System.out.println("Chunk #" + chunkNum + " bad checksum.");
            return;
        }
        for (int i = 0; i < checksumData.length; i++) {
            if (chunkVerify[i] != checksumData[i]) {
                socketOut.writeByte(Config.PH_CHUNK_ERROR);
                System.out.println("Chunk #" + chunkNum + " bad checksum.");
                return;
            }
        }

        // write to file
        fileOut.write(chunkData);
        // update state
        chunkNum++;
        bytesRead += chunkData.length;
        // indicate good chunk
        socketOut.writeByte(Config.PH_CHUNK_OK);

        // check if we are done
        if (chunkNum == numChunks) {
            fileOut.close();
            receiving = false;
            System.out.println("Done. Got " + bytesRead + " bytes.");
        }
    }

    private static void readN(DataInputStream in, byte[] buffer, int n) throws IOException{
        int read = 0;
        while (read < n) {
            read += in.read(buffer, read, n - read);
        }
        System.out.println("read:"+read);
    }
}