package server.wallet;

import common.Result;
import server.model.User;

public class WalletService {

    public Result<Void> charge(User user, double amount) {

        if (user == null) {
            return Result.error("User is null");
        }

        if (amount <= 0) {
            return Result.error("Amount must be greater than 0.");
        }

        user.getWallet().deposit(amount);

        return Result.success("Wallet charge successful");
    }

    public Result<Double> balance(User user) {
        if (user == null) {
            return Result.error("User is null.");
        }

        return Result.success("Balance", user.getWallet().getBalance());
    }
}
