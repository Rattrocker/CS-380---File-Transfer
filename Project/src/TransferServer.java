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
           // DataOutputStream socketOut = new DataOutputStream(clientSocket.getOutputStream());
            PrintWriter out =  new PrintWriter(clientSocket.getOutputStream(), true); //this does print to the client side.


            String inputLine;
            String outputLine;
        

            // Initiate conversation with client
            Protocol protocol = new Protocol();
            outputLine = protocol.processInput(null);                  
            out.println(outputLine);      

                               
            while ((inputLine = socketIn.readLine()) != null) { 
              out.println("");                                                              
              outputLine = protocol.processInput(inputLine);          
              out.println(outputLine);                                
              if (outputLine.equals("You've been authenticated! Good bye!")){
                authenticated = true;
                 break;
              }
              else {
                inputLine = socketIn.readLine();
              }          
              
            }
        }
    }


}