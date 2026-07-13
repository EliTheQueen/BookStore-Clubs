package server.command;

import common.Request;
import common.Result;
import server.auth.AuthService;

public class RegisterCommand implements CommandHandler {

    private final AuthService authService;

    public RegisterCommand(AuthService authService) {
        this.authService = authService;
    }


    @Override
    public Result<?> execute(Request request) {
        return authService.register(request.get("username"), request.get("password"));
    }
}
