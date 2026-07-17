package server.command;

import common.Request;
import common.Result;
import server.club.ClubService;
import server.model.User;
import server.session.SessionManager;

public class CreateClubCommand extends AuthorizedCommand {

    private final ClubService clubService;

    public CreateClubCommand(ClubService clubService, SessionManager sessionManager) {
        super(sessionManager);
        this.clubService = clubService;
    }

    @Override
    public Result<?> execute(Request request) {
        User user = getUser(request);
        return clubService.createClub(user, request.get("name"));
    }
}
