package server.command;

import common.Request;
import common.Result;
import server.club.ClubService;
import server.model.User;
import server.session.SessionManager;

public class JoinClubCommand extends AuthorizedCommand {

    private final ClubService clubService;

    public JoinClubCommand(ClubService clubService, SessionManager sessionManager) {
        super(sessionManager);
        this.clubService = clubService;
    }

    @Override
    public Result<?> execute(Request request) {
        User user = getUser(request);

        try {
            int clubId = Integer.parseInt(request.get("clubId"));
            return clubService.requestJoin(user, clubId);
        } catch (NumberFormatException exception) {
            return Result.error("Invalid club ID.");
        }
    }
}
