package  server.core;

import server.auth.AuthService;
import server.book.BookService;
import server.model.BookStore;
import server.repository.UserRepository;
import server.session.SessionManager;
import server.wallet.WalletService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    private static final int PORT = 8082;

    public static void main(String[] args) throws IOException {

        UserRepository userRepository = new UserRepository();

        SessionManager sessionManager = new SessionManager();

        BookStore bookStore = new BookStore();

        AuthService authService = new AuthService(userRepository, sessionManager);

        BookService bookService = new BookService(bookStore);

        WalletService walletService = new WalletService();

        CommandDispatcher dispatcher = new CommandDispatcher(authService, bookService, walletService, sessionManager);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();

                System.out.println("Client connected from " + socket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(socket, dispatcher);

                new Thread(clientHandler).start();
            }
        }

            //چرا Dispatcher را فقط یک بار ساختیم؟
            //اگر این کار را بکنی:
            //while(true){
            //ClientHandler handler = new ClientHandler(socket,new CommandDispatcher(...));
            //}
            //به ازای هر Client یک Dispatcher می‌سازی.
            //لازم نیست.
            //Dispatcher هیچ Stateای ندارد.
            //یک نمونه کافی است.

    }

}