import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;

public class MultiServer {
    private static Set<String> userNames = new HashSet<>();
    private static Set<UserThread> userThreads = new HashSet<>();

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: java KKMultiServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        boolean listening = true;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {
                UserThread userThread = new UserThread(serverSocket.accept());
                userThreads.add(userThread);
                userThread.start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }

    public static void broadcast(String message, UserThread excludeClient) {
        //System.out.println("broadcast "+message);
        for (UserThread user : userThreads) {
            if (user != excludeClient) {
                user.sendMessage(message);
            }
        }
    }

    public static boolean addUserName(String userName) {
        boolean b = userNames.add(userName);
        for (String user : userNames) {
            System.out.println(user);
        }
        return b;
    }

    public static void removeUser(String userName, UserThread userThread) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            userThreads.remove(userThread);
            System.out.println(userName + " has left");
            broadcast(userName + " has left the chat.", userThread);
        }
    }
}