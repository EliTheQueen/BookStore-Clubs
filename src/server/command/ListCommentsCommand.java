package server.command;

import common.Request;
import common.Result;
import server.club.ClubService;
import server.model.User;
import server.session.Session;
import server.session.SessionManager;

public class ListCommentsCommand extends SessionCommand {

    private final ClubService clubService;

    public ListCommentsCommand(ClubService clubService, SessionManager sessionManager) {
        super(sessionManager);
        this.clubService = clubService;
    }

    @Override
    public Result<?> execute(Request request) {
        Session session = getSession(request);
        User user = getUser(request);

        if (session == null || session.getViewedClubId() == null) {
            return Result.error("First use view_club.");
        }

        return clubService.listComments(user, session.getViewedClubId());
    }
}
