package server.command;

import common.Request;
import common.Result;
import server.book.BookService;
import server.model.User;
import server.session.SessionManager;

public class BookBuyCommand extends AuthorizedCommand {

    private final BookService bookService;

    public BookBuyCommand(BookService bookService, SessionManager sessionManager) {
        super(sessionManager);
        this.bookService = bookService;
    }

    @Override
    public Result<?> execute(Request request) {

        User user = getUser(request);

        if (user == null) {
            return Result.error("Unauthorized");
        }

        String bookIdText = request.get("bookId");

        if (bookIdText == null || bookIdText.isBlank()) {
            return Result.error("Book ID is required");
        }

        try {
            int bookId = Integer.parseInt(bookIdText);
            return bookService.buyBook(user, bookId);
        }
        catch (NumberFormatException exception) {
            return Result.error("Invalid book ID");
        }

    }
}
