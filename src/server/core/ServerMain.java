package  server.core;

import server.auth.AuthService;
import server.repository.UserRepository;
import server.session.SessionManager;

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

            UserRepository repository = new UserRepository();

            SessionManager sessionManager = new SessionManager();

            AuthService authService = new AuthService(repository, sessionManager);

            CommandDispatcher dispatcher = new CommandDispatcher(authService);

            //چرا Dispatcher را فقط یک بار ساختیم؟
            //اگر این کار را بکنی:
            //while(true){
            //ClientHandler handler = new ClientHandler(socket,new CommandDispatcher(...));
            //}
            //به ازای هر Client یک Dispatcher می‌سازی.
            //لازم نیست.
            //Dispatcher هیچ Stateای ندارد.
            //یک نمونه کافی است.
            ClientHandler handler = new ClientHandler(socket, dispatcher);
            new Thread(handler).start();
        }
    }

}