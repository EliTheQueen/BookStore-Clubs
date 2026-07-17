package server.notif;

import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationService {

    private final ConcurrentHashMap<String, PrintWriter> onlineUsers = new ConcurrentHashMap<>();

    public void register(String username, PrintWriter writer) {
        if (username == null || username.isBlank() || writer == null) {
            return;
        }
        onlineUsers.put(username, writer);
    }

    public void unregister(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        onlineUsers.remove(username);
    }

    public void sendToUser(String username, String message) {
        PrintWriter writer = onlineUsers.get(username);
        if (writer != null) {
            writer.println(notificationJson(message));
        }
    }

    public void sendToUsers(Set<String> usernames, String message) {
        if (usernames == null) {
            return;
        }
        for (String username : usernames) {
            sendToUser(username, message);
        }
    }

    private String notificationJson(String message) {
        String safe = message == null ? "" : message
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
        return "{\"status\":\"notification\",\"message\":\"" + safe + "\",\"data\":null}";
    }
}
