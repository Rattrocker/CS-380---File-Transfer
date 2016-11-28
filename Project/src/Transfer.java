import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by cthill on 10/31/16.
 */
public class Transfer {
    public static void main(String args[]) {
        List<String> argslist = Arrays.asList(args);

        if (argslist.size() < 1) {
            System.out.println("Usage:");
            System.out.println("\ttransfer [-s server] [-a asciiarmor] [-p port] [-x xorKeyFile] [-d drop random packets] [sourcefile host:destfile]");
            System.exit(1);
        }

        // read xor file if xor switch is enabled
        byte[] xorKey = new byte[Constants.CHUNK_SIZE];
        boolean enableXOR = false;

        if (argslist.indexOf("-x") != -1) {
            try {
                String xorFileName = argslist.get(argslist.indexOf("-x") + 1);
                File f = new File(xorFileName);
                new FileInputStream(f).read(xorKey);
                enableXOR = true;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error reading file: " + e.getMessage());
                System.exit(1);
            }
        }

        boolean serverMode = argslist.contains("-s");
        if (serverMode) {
            try {
                int port = 9999;

                if (argslist.indexOf("-p") != -1) {
                    port = Integer.parseInt(argslist.get(argslist.indexOf("-p") + 1));
                }

                TransferServer ts = new TransferServer(port, false);
                System.out.println("Listening on port " + port);

                ts.serve(xorKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String sourceFilename = "";
            String destFilename = "";
            String serverAddress = "";
            int port = 9999;
            boolean asciiArmor = false;
            boolean dropRandomPackets = false;
            int dropChance = 0;
            boolean xor = true;

            boolean firstFile = true;

            // parse arguments
            for (int i = 0; i < argslist.size(); i++) {
                String arg = argslist.get(i);

                if (arg.startsWith("-")) {
                    // port flag
                    if (arg.equals("-p")) {
                        port = Integer.parseInt(argslist.get(++i));
                    } 
                    // ascii armor flag
                    else if(arg.equals("-a")) {
                        asciiArmor = true;
                    } 
                    // force drop packet flag
                    else if(arg.equals("-d")) {
                        dropRandomPackets = true;
                        dropChance = Integer.parseInt(argslist.get(++i));
                        if (dropChance < 1) {
                            System.out.println("Minimum packet drop change is 1.");
                            System.exit(1);
                        }
                    }
                    // do not xor flag
                    else if(arg.equals("-x")) {
                        xor = false;
                    }

                } else {
                    //not flag
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
                tc.transfer(sourceFilename, destFilename, asciiArmor, enableXOR, xorKey, dropRandomPackets, dropChance);

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
