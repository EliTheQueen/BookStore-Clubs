package server.command;

import common.Request;
import common.Result;
import server.lending.LendingService;
import server.model.User;
import server.session.SessionManager;

public class AnswerLendRequestCommand extends AuthorizedCommand {

    private final LendingService lendingService;
    private final boolean accepted;

    public AnswerLendRequestCommand(LendingService lendingService,
                                    SessionManager sessionManager,
                                    boolean accepted) {
        super(sessionManager);
        this.lendingService = lendingService;
        this.accepted = accepted;
    }

    @Override
    public Result<?> execute(Request request) {
        User user = getUser(request);

        try {
            int requestId = Integer.parseInt(request.get("requestId"));
            if (accepted) {
                return lendingService.acceptRequest(user, requestId);
            }
            return lendingService.denyRequest(user, requestId);
        } catch (NumberFormatException exception) {
            return Result.error("Invalid request ID.");
        }
    }
}
