package server.command;

import common.Request;
import common.Result;
import server.model.User;
import server.session.SessionManager;
import server.wallet.WalletService;

public class BalanceCommand extends AuthorizedCommand {

    private final WalletService walletService;

    public BalanceCommand(WalletService walletService, SessionManager sessionManager) {
        super(sessionManager);
        this.walletService = walletService;
    }

    @Override
    public Result<?> execute(Request request){

        User user=getUser(request);

        if(user==null){

            return Result.error("Unauthorized.");

        }

        return walletService.balance(user);

    }
}
