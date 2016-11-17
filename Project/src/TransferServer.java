import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by cthill on 11/16/16.
 */
public class TransferServer {
    protected ServerSocket serverSocket;

    public TransferServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public void serve() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connect: " + clientSocket.getInetAddress());
            BufferedReader socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream socketOut = new DataOutputStream(clientSocket.getOutputStream());

            //TODO: everything...
        }
    }
}
