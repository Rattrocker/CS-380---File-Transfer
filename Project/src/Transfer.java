import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by cthill on 10/31/16.
 */
public class Transfer {
    public static void main(String args[]) {
        ArrayList<String> argslist = new ArrayList<String>(Arrays.asList(args));

        if (argslist.size() < 1) {
            System.out.println("Usage:");
            System.out.println("\ttransfer [-s server] [-a asciiarmor] [-p port] [-x xorKeyFile] [-d drop random packets] [sourcefile host:destfile]");
            System.exit(1);
        }

        // read xor file if xor switch is enabled
        byte[] xorKey = new byte[0];
        boolean enableXOR = false;
        int index = argslist.indexOf("-x");
        if (index != -1) {
            try {
                String xorFileName = argslist.get(index + 1);
                File f = new File(xorFileName);
                xorKey = new byte[(int) Math.min(f.length(), Constants.CHUNK_SIZE)];
                new FileInputStream(f).read(xorKey);
                enableXOR = true;

                // remove from arglist
                argslist.remove(index + 1);
                argslist.remove(index);
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
                System.exit(1);
            }
        }

        // read port
        int port = 9999;
        index = argslist.indexOf("-p");
        if (index != -1) {
            port = Integer.parseInt(argslist.get(index + 1));
            // remove from arglist
            argslist.remove(index + 1);
            argslist.remove(index);
        }

        // ascii armor
        boolean asciiArmor = false;
        index = argslist.indexOf("-a");
        if (index != -1) {
            asciiArmor = true;
            // remove from arglist
            argslist.remove(index);
        }

        // drop random packets switch
        int dropChance = -1;
        index = argslist.indexOf("-d");
        if (index != -1) {
            asciiArmor = true;
            dropChance = Integer.parseInt(argslist.get(index + 1));
            if (dropChance < 1) {
                System.out.println("Minimum packet drop change is 1.");
                System.exit(1);
            }
            // remove from arglist
            argslist.remove(index + 1);
            argslist.remove(index);
        }

        // server mode
        boolean serverMode = false;
        index = argslist.indexOf("-s");
        if (index != -1) {
            serverMode = true;
            // remove from arglist
            argslist.remove(index);
        }

        if (serverMode) {
            try {
                TransferServer ts = new TransferServer(port, false);
                System.out.println("Listening on port " + port);

                ts.serve(xorKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            boolean firstFile = true;
            String sourceFilename = "";
            String destFilename = "";
            String serverAddress = "";

            // parse arguments
            for (int i = 0; i < argslist.size(); i++) {
                String arg = argslist.get(i);

                if (arg.startsWith("-")) {
                    // switch
                    System.out.println("Unknown switch: " + arg);
                    System.exit(1);
                } else {
                    //not switch
                    if (firstFile) {
                        sourceFilename = arg;
                        firstFile = false;
                    } else {
                        String[] split = arg.split(":");
                        serverAddress = split[0];

                        if (split.length > 1) {
                            destFilename = split[1];
                        } else {
                            destFilename = sourceFilename;
                        }
                    }
                }
            }


            TransferClient tc;
            try {
                // connect to server
                tc = new TransferClient(serverAddress, port);
            } catch (IOException e) {
                System.out.println("Error connecting to " + serverAddress + ": " + e.getMessage());
                return;
            }

            try {
                // authenticate
                Scanner in = new Scanner(System.in);

                int attempts = 0;
                while (true) {
                    // read username and pass
                    System.out.print(" Username: ");
                    String user = in.nextLine();
                    System.out.print(" Password: ");
                    String pass = new String(System.console().readPassword());

                    // try auth. If good, break out of loop
                    if (tc.authenticate(user, pass)) {
                        break;
                    }

                    System.out.println("Bad login.");

                    // if max attempts, break out of loop. server will close connection
                    attempts++;
                    if (attempts >= Constants.MAX_AUTH_ATTEMPTS) {
                        System.exit(1);
                    }
                }

                // transfer file
                tc.transfer(sourceFilename, destFilename, asciiArmor, enableXOR, xorKey, dropChance);

                // close connection
                tc.disconnect();

                System.out.println("Done.");
            } catch (EOFException e) {
                System.out.println("Connection closed by server.");
                tc.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                tc.close();
            }
        }
    }
}
