package server.core;

import javax.sound.sampled.Port;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    private ServerSocket serverSocket;
    private Port port;

    public void start() throws Exception {
        this.serverSocket = new ServerSocket();
        System.out.println("Server started on port: " + this.serverSocket.getLocalPort());

        while(true){
            Socket socket = this.serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler();
            Thread thread = new Thread();
            thread.start();
        }
    }
}
