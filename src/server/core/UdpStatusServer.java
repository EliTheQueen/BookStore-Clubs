package server.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class UdpStatusServer implements Runnable {

    private final int port;

    public UdpStatusServer(int port) {
        this.port = port;
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
}
