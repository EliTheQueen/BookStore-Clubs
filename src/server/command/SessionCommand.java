package server.command;

import common.Request;
import server.model.User;
import server.session.Session;
import server.session.SessionManager;

public abstract class SessionCommand extends AuthorizedCommand {

    protected SessionCommand(SessionManager sessionManager) {
        super(sessionManager);
    }

    protected Session getSession(Request request) {
        if (request == null) {
            return null;
        }
        return sessionManager.getSession(request.getToken());
    }

    @Override
    protected User getUser(Request request) {
        Session session = getSession(request);
        if (session == null) {
            return null;
        }
        return session.getUser();
    }
}
