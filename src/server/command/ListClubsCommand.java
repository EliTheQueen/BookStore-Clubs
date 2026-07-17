package server.command;

import common.Request;
import common.Result;
import server.club.ClubService;
import server.model.User;
import server.session.SessionManager;

public class ListClubsCommand extends AuthorizedCommand {

    private final ClubService clubService;
    private final boolean allClubs;

    public ListClubsCommand(ClubService clubService, SessionManager sessionManager, boolean allClubs) {
        super(sessionManager);
        this.clubService = clubService;
        this.allClubs = allClubs;
    }

    @Override
    public Result<?> execute(Request request) {
        User user = getUser(request);

        if (allClubs) {
            return clubService.listAllClubs(user);
        }
        return clubService.listMyClubs(user);
    }
}
