package server.command;

import common.Request;
import common.Result;
import server.auth.AuthService;

public class LoginCommand implements CommandHandler {

    private final AuthService authService;

    public LoginCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Result<?> execute(Request request) {
        return authService.login(request.get("username"), request.get("password"));
    }
}
