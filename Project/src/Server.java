import java.net.*;
import java.io.*;

public class Server {
    public static void main(String[] args) throws IOException {
        
        if (args.length != 1) {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);

        try ( 
            //creates the ServerSocket object to listen on a specific port
            ServerSocket serverSocket = new ServerSocket(portNumber);   //if successful binding to the port (i.e. the port isn't being used)
            Socket clientSocket = serverSocket.accept();                //it can now accept connection from client
            PrintWriter out =                                           //accept() waits until client starts up and requests connection on the host and port of this server    
                new PrintWriter(clientSocket.getOutputStream(), true);  //sends info to client socket
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())); //reads what client socket is saying
        ) {
        
            String inputLine, outputLine;
            
            // Initiate conversation with client
            Protocol protocol = new Protocol();
            outputLine = protocol.processInput(null);                  //it goes into protocol and into processInput() for the first time.
            out.println(outputLine);                                   //now it prints out "Please enter your username"     

            while ((inputLine = in.readLine()) != null) {               //while the client has something to say
                outputLine = protocol.processInput(inputLine);          //sends in what the client said to be checked in the Protocol class
                out.println(outputLine);                                //prints out the appropriate response from Protocol class
                if (outputLine.equals("You've been authenticated! Good bye!"))          //if you've been authenticated, close the connection on the server side!
                    break;
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}