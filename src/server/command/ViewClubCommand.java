package server.command;

import common.Request;
import common.Result;
import server.club.ClubService;
import server.model.User;
import server.session.Session;
import server.session.SessionManager;

public class ViewClubCommand extends SessionCommand {

    private final ClubService clubService;

    public ViewClubCommand(ClubService clubService, SessionManager sessionManager) {
        super(sessionManager);
        this.clubService = clubService;
    }

    @Override
    public Result<?> execute(Request request) {
        Session session = getSession(request);
        User user = getUser(request);

        if (session == null) {
            return Result.error("Unauthorized.");
        }

        try {
            int clubId = Integer.parseInt(request.get("clubId"));
            Result<?> result = clubService.viewClub(user, clubId);
            if (result.isSuccess()) {
                session.setViewedClubId(clubId);
            }
            return result;
        } catch (NumberFormatException exception) {
            return Result.error("Invalid club ID.");
        }
    }
}
