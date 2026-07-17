package server.command;

import common.Request;
import common.Result;
import server.club.ClubService;
import server.model.User;
import server.session.Session;
import server.session.SessionManager;

public class AnswerJoinRequestCommand extends SessionCommand {

    private final ClubService clubService;
    private final boolean accepted;

    public AnswerJoinRequestCommand(ClubService clubService, SessionManager sessionManager, boolean accepted) {
        super(sessionManager);
        this.clubService = clubService;
        this.accepted = accepted;
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

        if (accepted) {
            return clubService.acceptJoin(user, session.getViewedClubId(), username);
        }
        return clubService.denyJoin(user, session.getViewedClubId(), username);
    }
}
