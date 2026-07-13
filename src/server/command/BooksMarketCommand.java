package server.command;

import common.Request;
import common.Result;
import server.book.BookService;
import server.model.User;
import server.session.SessionManager;

public class BooksMarketCommand extends AuthorizedCommand {

    private final BookService bookService;

    public BooksMarketCommand(BookService bookService, SessionManager sessionManager) {
        super(sessionManager);
        this.bookService = bookService;
    }

    @Override
    public Result<?> execute(Request request) {

        User user = getUser(request);

        if (user == null) {
            return Result.error("Unauthorized.");
        }

        return bookService.marketList();
    }
}