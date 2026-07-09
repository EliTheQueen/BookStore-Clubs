package server.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final CommandDispatcher dispatcher;

    private BufferedReader reader;

    private PrintWriter writer;

    public ClientHandler(Socket socket) throws IOException {

        this.socket = socket;
        this.dispatcher = new CommandDispatcher();

        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        writer = new PrintWriter(
                socket.getOutputStream(), true);

    }

    @Override
    public void run() {

        try {

            String line;

            while ((line = reader.readLine()) != null) {

                System.out.println("Client says: " + line);

                String response = dispatcher.dispatch(line);

                writer.println(response);
            }

        } catch (IOException e) {

            System.out.println("Client disconnected.");

        }

    }

}