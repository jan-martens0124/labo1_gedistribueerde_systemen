import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class UserThread extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String userName;

    public UserThread(Socket socket) {
        super("UserThread");
        this.socket = socket;
    }

    public void run() {

        try (
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()));
        ) {
            this.out = pw;
            this.in = br;
            out.println("Enter your username:");
            userName = in.readLine();
            if (!MultiServer.addUserName(userName)) socket.close();
            MultiServer.broadcast(userName + " has joined", this);

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                MultiServer.broadcast("[" + userName + "]: " + clientMessage, this);
            }
            MultiServer.removeUser(userName, this);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}