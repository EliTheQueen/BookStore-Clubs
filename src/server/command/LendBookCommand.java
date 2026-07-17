package server.command;

import common.Request;
import common.Result;
import server.lending.LendingService;
import server.model.User;
import server.session.SessionManager;

public class LendBookCommand extends AuthorizedCommand {

    private final LendingService lendingService;

    public LendBookCommand(LendingService lendingService, SessionManager sessionManager) {
        super(sessionManager);
        this.lendingService = lendingService;
    }

    @Override
    public Result<?> execute(Request request) {
        User user = getUser(request);

        try {
            int bookId = Integer.parseInt(request.get("bookId"));
            return lendingService.lendBook(user, bookId, request.get("username"));
        } catch (NumberFormatException exception) {
            return Result.error("Invalid book ID.");
        }
    }
}
