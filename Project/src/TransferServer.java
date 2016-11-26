import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

/**
 * Created by cthill on 11/16/16.
 */
public class TransferServer {
    protected ServerSocket serverSocket;
    protected boolean allowAnon;
    protected boolean authenticated;

    public TransferServer(int port, boolean allowAnon) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.allowAnon = allowAnon;
        this.authenticated = false;
    }

    public void serve() throws IOException {
        // server only accepts one client at a time
        // TODO: implement threading
        while (true) {
            Socket clientSocket = serverSocket.accept(); // blocks until a client connects
            System.out.println("Client connect: " + clientSocket.getInetAddress());
            DataInputStream socketIn = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream socketOut = new DataOutputStream(clientSocket.getOutputStream());

            // TODO: replace literal "login.txt" with command line parameter
            ServerProtocol protocol = new ServerProtocol(socketIn, socketOut, "login.txt");
            protocol.run();
        }
    }


}