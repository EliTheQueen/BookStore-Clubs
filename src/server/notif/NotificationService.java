package server.notif;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationService {

    private final ConcurrentHashMap<String, UdpClient> udpClients = new ConcurrentHashMap<>();

    public void registerUdp(String username, InetAddress address, int port) {
        if (username == null || username.isBlank() || address == null || port <= 0) {
            return;
        }
        udpClients.put(username, new UdpClient(address, port));
    }

    public void unregister(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        udpClients.remove(username);
    }

    public void sendToUser(String username, String message) {
        UdpClient client = udpClients.get(username);
        if (client == null) {
            return;
        }

        byte[] bytes = notificationJson(message).getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, client.address, client.port);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(packet);
        } catch (IOException exception) {
            System.out.println("Notification failed for " + username + ": " + exception.getMessage());
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

    private static class UdpClient {
        private final InetAddress address;
        private final int port;

        private UdpClient(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }
    }
}
