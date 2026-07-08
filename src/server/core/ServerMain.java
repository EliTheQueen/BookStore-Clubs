package  server.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    private static final int PORT = 8082;

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(PORT);

        System.out.println("Server started on port " + PORT);

        while (true) {

            Socket socket = serverSocket.accept();

            System.out.println("Client Connected : " + socket.getInetAddress());

            ClientHandler handler = new ClientHandler(socket);

            new Thread(handler).start();
        }
    }

}