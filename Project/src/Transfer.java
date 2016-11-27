import java.io.IOException;
import java.net.ServerSocket;
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
            System.out.println("\ttransfer [-s server] [-p port] [-x do not xor] [-a ascii armor] [-d drop random packets] [sourcefile host:destfile]");
            System.exit(1);
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
                ts.serve();
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
            int packetsToDrop = 0;
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
                        packetsToDrop = Integer.parseInt(argslist.get(++i));
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

            try {
                // connect to server
                TransferClient tc = new TransferClient(serverAddress, port);

                // authenticate
                Scanner in = new Scanner(System.in);
                int attempts = 0;
                while (attempts < Constants.MAX_AUTH_ATTEMPTS) {
                    System.out.print("username: ");
                    String user = in.nextLine();
                    System.out.print("password: ");
                    String pass = new String(System.console().readPassword());
                    if (tc.authenticate(user, pass)) {
                        break;
                    }
                    System.out.println("Bad login, try again.");
                    attempts++;
                }

                // transfer file
                tc.transfer(sourceFilename, destFilename, asciiArmor, xor, dropRandomPackets, packetsToDrop);

                // close connection
                tc.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
