import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.net.SocketException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;


/**
 * Created by cthill on 10/31/16.
 */
public class Transfer {
    public static void main(String args[]) {
        List<String> argslist = Arrays.asList(args);

        if (argslist.size() < 1) {
            System.out.println("Usage:");
            System.out.println("\ttransfer [-s server] [-p port] [-x xorKeyFile] [sourcefile host:destfile]");
            System.exit(1);
        }

        //String xorFileName="";
        byte[] xorKeyFile = new byte[0];
        boolean enableXOR = false;

        if (argslist.indexOf("-x") != -1) {
            try {
                String xorFileName = argslist.get(argslist.indexOf("-x") + 1);
                Path path = Paths.get(xorFileName);
                xorKeyFile = Files.readAllBytes(path);
                enableXOR = true;

            }
            catch (IOException e) {
                e.printStackTrace();
                enableXOR = false;
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

                ts.serve(xorKeyFile, enableXOR);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String sourceFilename = "";
            String destFilename = "";
            String serverAddress = "";
            int port = 9999;

            boolean firstFile = true;

            // parse arguments
            for (int i = 0; i < argslist.size(); i++) {
                String arg = argslist.get(i);

                if (arg.startsWith("-")) {
                    //port flag
                    if (arg.equals("-p")) {
                        port = Integer.parseInt(argslist.get(++i));
                    }

                    //todo: implement flags for base64, encryption, etc
                } else {
                    //not flag
                    if (firstFile) {
                        sourceFilename = argslist.get(++i);
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
                tc.transfer(sourceFilename, destFilename, xorKeyFile, enableXOR);

                // close connection
                tc.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }System.out.println("transfer name: " + sourceFilename);
        }
    }
}
