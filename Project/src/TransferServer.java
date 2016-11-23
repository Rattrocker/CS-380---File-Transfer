import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

/**
 * Created by cthill on 11/16/16.
 */
public class TransferServer {
    protected ServerSocket serverSocket;
    private boolean authenticated;

    public TransferServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        authenticated = false;
    }

    public void serve() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connect: " + clientSocket.getInetAddress());
            BufferedReader socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream socketOut = new DataOutputStream(clientSocket.getOutputStream());

            // authenticate
            if(!authenticated) {
            //TODO: everything...                
            }
            // else already authenticated
            else {
                            PrintWriter out =                           //accept() waits until client starts up and requests connection on the host and port of this server    
                new PrintWriter(clientSocket.getOutputStream(), true);  //sends info to client socket

                String inputLine;
                String outputLine;
            
                // Initiate conversation with client
                Protocol protocol = new Protocol();
                outputLine = protocol.processInput(null);                  //it goes into protocol and into processInput() for the first time.
                out.println(outputLine);                                   //now it prints out "Please enter your username"     

                while ((inputLine = socketIn.readLine()) != null) {               //while the client has something to say
                  outputLine = protocol.processInput(inputLine);          //sends in what the client said to be checked in the Protocol class
                  out.println(outputLine);                                //prints out the appropriate response from Protocol class
                  if (outputLine.equals("You've been authenticated! Good bye!"))          //if you've been authenticated, close the connection on the server side!
                     authenticated = true;
                     break;
                }
            }
        }
    }


}