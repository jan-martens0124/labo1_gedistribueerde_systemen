import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class UserThread extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String userName;

    public UserThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("Enter your username:");
            userName = in.readLine();
            if (!MultiServer.addUserName(userName)) {
                socket.close();
                return;
            }
            MultiServer.broadcast(userName + " has joined", this);

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                if (clientMessage.startsWith("/private")) {
                    handlePrivateGroupCommand(clientMessage);
                } else if (clientMessage.startsWith("/switch")) {
                    MultiServer.switchToPublic(this);
                } else {
                    MultiServer.broadcast("[" + userName + "]: " + clientMessage, this);
                }
            }
            MultiServer.removeUser(userName, this);
            socket.close();
        }catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void handlePrivateGroupCommand(String clientMessage) {
        String[] tokens = clientMessage.split(" ");
        if (tokens.length < 2) {
            sendMessage("Usage: /private [user1,user2,...]");
            return;
        }

        String groupName = "private-" + userName +" - " + tokens[1]; // Unique group name
        Set<UserThread> groupMembers = new HashSet<>();
        groupMembers.add(this); // Add self to the group

        // Add other users by their username
        for (int i = 1; i < tokens.length; i++) {
            UserThread user = getUserByUsername(tokens[i]);
            if (user != null) {
                groupMembers.add(user);
            }
        }

        MultiServer.createPrivateGroup(groupName, groupMembers);
    }

    private UserThread getUserByUsername(String username) {
        for (UserThread userThread : MultiServer.userThreads) {
            if (userThread.userName.equals(username)) {
                return userThread;
            }
        }
        return null;
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
