package server.command;

import common.Request;
import common.Result;
import server.club.ClubService;
import server.model.User;
import server.session.Session;
import server.session.SessionManager;

public class RemoveMemberCommand extends SessionCommand {

    private final ClubService clubService;

    public RemoveMemberCommand(ClubService clubService, SessionManager sessionManager) {
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

        String username = request.get("username");
        if (username == null) {
            username = request.get("userId");
        }

        return clubService.removeMember(user, session.getViewedClubId(), username);
    }
}
