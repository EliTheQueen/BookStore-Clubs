package server.command;

import common.Request;
import common.Result;
import server.model.User;
import server.session.Session;
import server.session.SessionManager;

public abstract class AuthorizedCommand implements CommandHandler {

    protected final SessionManager sessionManager;

    protected AuthorizedCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    protected User getUser(Request request) {

        if (request == null) {
            return null;
        }

        Session session = sessionManager.getSession(request.getToken());

        if (session == null) {
            return null;
        }

        return session.getUser();
    }
}
