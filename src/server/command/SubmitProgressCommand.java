package server.command;

import common.Request;
import common.Result;
import server.model.User;
import server.progress.ProgressService;
import server.session.SessionManager;

public class SubmitProgressCommand extends AuthorizedCommand {

    private final ProgressService progressService;

    public SubmitProgressCommand(ProgressService progressService, SessionManager sessionManager) {
        super(sessionManager);
        this.progressService = progressService;
    }

    @Override
    public Result<?> execute(Request request) {
        User user = getUser(request);

        if (user == null) {
            return Result.error("Unauthorized.");
        }

        try {
            int bookId = Integer.parseInt(request.get("bookId"));
            int page = Integer.parseInt(request.get("page"));
            return progressService.submitProgress(user, bookId, page);
        } catch (NumberFormatException exception) {
            return Result.error("Invalid book ID or page.");
        }
    }
}
