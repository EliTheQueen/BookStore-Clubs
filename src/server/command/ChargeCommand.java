package server.command;

import common.Request;
import common.Result;
import server.model.User;
import server.session.SessionManager;
import server.wallet.WalletService;

public class ChargeCommand extends AuthorizedCommand {

    private final WalletService walletService;

    public ChargeCommand(WalletService walletService, SessionManager sessionManager) {
        super(sessionManager);
        this.walletService = walletService;
    }

    @Override
    public Result<?> execute(Request request) {
        User user = getUser(request);

        if (user == null) {
            return Result.error("Unauthorized");
        }

        try {
            double amount = Double.parseDouble(request.get("amount"));

            return walletService.charge(user, amount);
        } catch (NumberFormatException e) {
            return Result.error("Invalid amount");
        }
    }
}
