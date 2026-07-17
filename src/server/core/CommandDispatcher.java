package server.core;

import common.Request;
import common.Result;
import server.auth.AuthService;
import server.book.BookService;
import server.command.*;
import server.model.BookLibraryStatus;
import server.progress.ProgressService;
import server.session.SessionManager;
import server.wallet.WalletService;

import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher {

    private final Map<String, CommandHandler> commands;

    public CommandDispatcher(
            AuthService authService,
            BookService bookService,
            ProgressService progressService,
            WalletService walletService,
            SessionManager sessionManager) {

        commands = new HashMap<>();

        commands.put("register", new RegisterCommand(authService));

        commands.put("login", new LoginCommand(authService));

        commands.put("logout", new LogoutCommand(authService));

        commands.put("books_market_list", new BooksMarketCommand(bookService, sessionManager));
        commands.put("list_market_books", new BooksMarketCommand(bookService, sessionManager));

        commands.put("book_buy", new BookBuyCommand(bookService, sessionManager));
        commands.put("buy_book", new BookBuyCommand(bookService, sessionManager));

        commands.put("account_charge", new ChargeCommand(walletService, sessionManager));
        commands.put("charge_account", new ChargeCommand(walletService, sessionManager));

        commands.put("balance_show", new BalanceCommand(walletService, sessionManager));
        commands.put("show_balance", new BalanceCommand(walletService, sessionManager));

        commands.put("submit_progress", new SubmitProgressCommand(progressService, sessionManager));

        commands.put("list_not_read_library", new ListLibraryCommand(
                progressService, sessionManager, BookLibraryStatus.BookStatus.NOT_READ));
        commands.put("list_reading_library", new ListLibraryCommand(
                progressService, sessionManager, BookLibraryStatus.BookStatus.READING));
        commands.put("list_read_library", new ListLibraryCommand(
                progressService, sessionManager, BookLibraryStatus.BookStatus.READ));
    }

    public Result<?> dispatch(Request request) {

        if (request == null) {
            return Result.error("Invalid request.");
        }

        String command = request.getCommand();

        if (command == null || command.isBlank()) {
            return Result.error("Command is missing.");
        }

        CommandHandler commandHandler = commands.get(command);

        if (commandHandler == null) {
            return Result.error("Unknown command.");
        }

        return commandHandler.execute(request);
    }
}
