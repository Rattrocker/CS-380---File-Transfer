import java.io.*;
import java.net.Socket;

/**
 * Created by cthill on 11/16/16.
 */
public class TransferClient {
    protected Socket socket;
    protected BufferedReader socketIn;
    protected DataOutputStream socketOut;

    public TransferClient(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.socketOut = new DataOutputStream(socket.getOutputStream());
    }

    public void transfer(String sourceFilename, String destFilename) {
        System.out.println("Not implemented...");
    }

    public void disconnect() throws IOException {
        socket.close();
    }
}
