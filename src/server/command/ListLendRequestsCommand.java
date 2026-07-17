package server.command;

import common.Request;
import common.Result;
import server.lending.LendingService;
import server.model.User;
import server.session.SessionManager;

public class ListLendRequestsCommand extends AuthorizedCommand {

    private final LendingService lendingService;

    public ListLendRequestsCommand(LendingService lendingService, SessionManager sessionManager) {
        super(sessionManager);
        this.lendingService = lendingService;
    }

    @Override
    public Result<?> execute(Request request) {
        User user = getUser(request);
        return lendingService.listPendingRequests(user);
    }
}
