package server.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class UdpStatusServer implements Runnable {

    private final int port;
    private final CommandDispatcher dispatcher;

    public UdpStatusServer(int port, CommandDispatcher dispatcher) {
        this.port = port;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[1024];
            System.out.println("UDP status server started on port " + port);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8).trim();
                String answer;
                if ("ping".equalsIgnoreCase(message) || "status".equalsIgnoreCase(message)) {
                    answer = "server is running";
                } else if (message.startsWith("register_notification:")) {
                    answer = registerNotification(message, packet);
                } else {
                    answer = "unknown udp message";
                }

                byte[] responseBytes = answer.getBytes(StandardCharsets.UTF_8);
                DatagramPacket response = new DatagramPacket(
                        responseBytes,
                        responseBytes.length,
                        packet.getAddress(),
                        packet.getPort());
                socket.send(response);
            }
        } catch (IOException exception) {
            System.out.println("UDP server stopped: " + exception.getMessage());
        }
    }

    private String registerNotification(String message, DatagramPacket packet) {
        String[] parts = message.split(":");
        if (parts.length != 3) {
            return "invalid notification registration";
        }

        try {
            int clientPort = Integer.parseInt(parts[2]);
            boolean registered = dispatcher.registerUdpNotificationClient(parts[1], packet.getAddress(), clientPort);
            if (registered) {
                return "notification listener registered";
            }
            return "invalid token";
        } catch (NumberFormatException exception) {
            return "invalid udp port";
        }
    }
}
