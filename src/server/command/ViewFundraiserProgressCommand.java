package server.command;

import common.Request;
import common.Result;
import server.fundraiser.FundraiserService;
import server.model.User;
import server.session.Session;
import server.session.SessionManager;

public class ViewFundraiserProgressCommand extends SessionCommand {

    private final FundraiserService fundraiserService;

    public ViewFundraiserProgressCommand(FundraiserService fundraiserService, SessionManager sessionManager) {
        super(sessionManager);
        this.fundraiserService = fundraiserService;
    }

    @Override
    public Result<?> execute(Request request) {
        Session session = getSession(request);
        User user = getUser(request);

        if (session == null || session.getViewedClubId() == null) {
            return Result.error("First use view_club.");
        }

        return fundraiserService.viewProgress(user, session.getViewedClubId());
    }
}
