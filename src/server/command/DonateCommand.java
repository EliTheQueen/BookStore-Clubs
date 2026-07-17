package server.command;

import common.Request;
import common.Result;
import server.fundraiser.FundraiserService;
import server.model.User;
import server.session.Session;
import server.session.SessionManager;

public class DonateCommand extends SessionCommand {

    private final FundraiserService fundraiserService;

    public DonateCommand(FundraiserService fundraiserService, SessionManager sessionManager) {
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

        try {
            double amount = Double.parseDouble(request.get("amount"));
            return fundraiserService.donate(user, session.getViewedClubId(), amount);
        } catch (NumberFormatException exception) {
            return Result.error("Invalid amount.");
        }
    }
}
