package server.core;

import common.Request;
import common.Result;
import server.auth.AuthService;
import server.book.BookService;
import server.command.*;
import server.session.SessionManager;
import server.wallet.WalletService;

import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher {

    private final Map<String, CommandHandler> commands;

    public CommandDispatcher(
            AuthService authService,
            BookService bookService,
            WalletService walletService,
            SessionManager sessionManager) {

        commands = new HashMap<>();

        commands.put("register", new RegisterCommand(authService));

        commands.put("login", new LoginCommand(authService));

        commands.put("logout", new LogoutCommand(authService));

        commands.put("books_market_list", new BooksMarketCommand(bookService, sessionManager));

        commands.put("book_buy", new BookBuyCommand(bookService, sessionManager));

        commands.put("account_charge", new ChargeCommand(walletService, sessionManager));

        commands.put("balance_show", new BalanceCommand(walletService, sessionManager));
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