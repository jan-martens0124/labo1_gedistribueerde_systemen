import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiServer {
    private static Set<String> userNames = new HashSet<>();
    static Set<UserThread> userThreads = new HashSet<>();
    private static Map<String, Set<UserThread>> privateGroups = new HashMap<>();
    private static Map<UserThread, String> userGroupMap = new HashMap<>();

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

    // Method to create or join a private chat group
    public static synchronized void createPrivateGroup(String groupName, Set<UserThread> users) {
        privateGroups.putIfAbsent(groupName, new HashSet<>());
        privateGroups.get(groupName).addAll(users);
        for (UserThread user : users) {
            userGroupMap.put(user, groupName);
            user.sendMessage("You have joined the private group: " + groupName);
        }
    }

    // Broadcast message to a specific group or public chat
    public static synchronized void broadcast(String message, UserThread excludeClient) {
        String groupName = userGroupMap.getOrDefault(excludeClient, "public");
        Set<UserThread> group = groupName.equals("public") ? userThreads : privateGroups.get(groupName);

        for (UserThread user : group) {
            if (user != excludeClient) {
                user.sendMessage(message);
            }
        }
    }

    public static synchronized void switchToPublic(UserThread user) {
        userGroupMap.put(user, "public");
    }

    public static synchronized void removeUser(String userName, UserThread userThread) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            userThreads.remove(userThread);
            broadcast(userName + " has left the chat.", userThread);
            privateGroups.values().forEach(group -> group.remove(userThread));
            userGroupMap.remove(userThread);
        }
    }

    public static boolean addUserName(String userName) {
        boolean b = userNames.add(userName);
        for (String user : userNames) {
            System.out.println(user);
        }
        return b;
    }

}
