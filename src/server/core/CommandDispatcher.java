package server.core;

import common.Request;
import common.Result;
import server.auth.AuthService;
import server.command.CommandHandler;
import server.command.LoginCommand;
import server.command.LogoutCommand;
import server.command.RegisterCommand;

import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher {

    private final Map<String, CommandHandler> commands;

    public  CommandDispatcher(AuthService authService) {

        commands = new HashMap<>();

        commands.put("register", new RegisterCommand(authService));

        commands.put("login", new LoginCommand(authService));

        commands.put("logout", new LogoutCommand(authService));
    }

    public Result<?> dispatch(Request request) {
        if (request == null) {
            return Result.error("Invalid request");
        }

        CommandHandler commandHandler = commands.get(request.getCommand());

        if (commandHandler == null) {
            return Result.error("Unknown command");
        }

        return commandHandler.execute(request);
    }
}