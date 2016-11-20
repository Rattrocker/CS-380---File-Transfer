import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        
        if (args.length != 2) {
            System.err.println(
                "Usage: java Client localhost <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
            Socket socket = new Socket(hostName, portNumber);                       //connects to the server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);      //"out" prints what the user types.
            BufferedReader in = new BufferedReader(                                 //"in" reads from the server
                new InputStreamReader(socket.getInputStream()));
        ) {
            BufferedReader stdIn =
                new BufferedReader(new InputStreamReader(System.in));               //"stdIn" reads in what the user types.
            String fromServer;
            String fromUser;

            while ((fromServer = in.readLine()) != null) {                          //while the server has something to say
                System.out.println("Server: " + fromServer);                        //print out what the server says
                if (fromServer.equals("You've been authenticated! Good bye!"))      //if you have been authenticated, close the connection on client side!
                    break;
                    
                fromUser = stdIn.readLine();                                        //read in what the user typed from keyboard
                if (fromUser != null) {
                    out.println(fromUser);                                          //echos on the screen what user is typing
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        }
    }
}