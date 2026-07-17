package client;

import common.JsonWriter;
import common.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClientMain {

    private static final String HOST = "localhost";
    private static final int TCP_PORT = 8082;
    private static final int UDP_PORT = 8083;

    private static volatile boolean running = true;
    private static volatile String token;

    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket(HOST, TCP_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            Thread listener = new Thread(() -> listen(reader));
            listener.setDaemon(true);
            listener.start();

            while (running) {
                String line = scanner.nextLine();
                if (line == null) {
                    break;
                }
                if ("exit".equalsIgnoreCase(line.trim())) {
                    running = false;
                    break;
                }
                if ("udp_ping".equalsIgnoreCase(line.trim())) {
                    System.out.println(sendUdp("ping"));
                    continue;
                }

                Request request = parseCommandLine(line);
                if (request == null) {
                    System.out.println("Invalid command.");
                    continue;
                }
                if (token != null) {
                    request.setToken(token);
                }

                writer.println(JsonWriter.write(request));
            }
        }
    }

    private static void listen(BufferedReader reader) {
        try {
            String response;
            while (running && (response = reader.readLine()) != null) {
                System.out.println(response);
                String foundToken = extractDataToken(response);
                if (foundToken != null) {
                    token = foundToken;
                    System.out.println("Token saved.");
                }
            }
        } catch (IOException exception) {
            if (running) {
                System.out.println("Disconnected from server.");
            }
        }
    }

    private static Request parseCommandLine(String line) {
        line = line.trim();
        if (line.isBlank()) {
            return null;
        }

        Request request = new Request();
        int open = line.indexOf('(');
        int close = line.lastIndexOf(')');

        if (open == -1) {
            request.setCommand(line);
            return request;
        }

        request.setCommand(line.substring(0, open).trim());
        if (close < open) {
            return null;
        }

        String args = line.substring(open + 1, close).trim();
        if (args.isBlank()) {
            return request;
        }

        String[] values = args.split(",");
        fillPayload(request, values);
        return request;
    }

    private static void fillPayload(Request request, String[] values) {
        String command = request.getCommand();

        if ("register".equals(command) || "login".equals(command)) {
            put(request, "username", values, 0);
            put(request, "password", values, 1);
        } else if ("create_club".equals(command)) {
            put(request, "name", values, 0);
        } else if ("join".equals(command) || "view_club".equals(command)) {
            put(request, "clubId", values, 0);
        } else if ("accept_join_request".equals(command)
                || "deny_join_request".equals(command)
                || "remove_member".equals(command)) {
            put(request, "username", values, 0);
        } else if ("charge_account".equals(command) || "donate".equals(command)) {
            put(request, "amount", values, 0);
        } else if ("buy_book".equals(command) || "create_fundraiser".equals(command)) {
            put(request, "bookId", values, 0);
        } else if ("submit_progress".equals(command)) {
            put(request, "bookId", values, 0);
            put(request, "page", values, 1);
        } else if ("lend_book".equals(command)) {
            put(request, "bookId", values, 0);
            put(request, "username", values, 1);
        } else if ("add_comment".equals(command)) {
            put(request, "text", values, 0);
        }
    }

    private static void put(Request request, String key, String[] values, int index) {
        if (index < values.length) {
            request.put(key, values[index].trim());
        }
    }

    private static String sendUdp(String message) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(HOST), UDP_PORT);
            socket.send(packet);

            byte[] buffer = new byte[1024];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);
            return new String(response.getData(), 0, response.getLength(), StandardCharsets.UTF_8);
        }
    }

    private static String extractDataToken(String response) {
        String marker = "\"data\":\"";
        int start = response.indexOf(marker);
        if (start == -1 || !response.contains("\"status\":\"success\"")) {
            return null;
        }
        start += marker.length();
        int end = response.indexOf("\"", start);
        if (end == -1) {
            return null;
        }
        String value = response.substring(start, end);
        if (value.length() < 20) {
            return null;
        }
        return value;
    }
}
