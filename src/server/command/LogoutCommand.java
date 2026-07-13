package server.command;

import common.Request;
import common.Result;
import server.auth.AuthService;

public class LogoutCommand implements CommandHandler {

    private final AuthService authService;

    public LogoutCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Result<?> execute(Request request) {
        return authService.logout(request.getToken());
    }
}
