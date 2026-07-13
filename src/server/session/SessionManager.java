package server.session;

import server.model.User;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final ConcurrentHashMap<String, Session> tokenToSession;

    private final ConcurrentHashMap<String, Session> usernameToSession;

    private final SecureRandom random;

    public SessionManager() {
        tokenToSession = new ConcurrentHashMap<>();
        usernameToSession = new ConcurrentHashMap<>();
        random = new SecureRandom();
    }

    public Session createSession(User user) {

        //هر کاربر فقط یک Session فعال دارد.
        Session oldSession = usernameToSession.get(user.getUsername());

        if (oldSession != null) {
            removeSession(oldSession.getToken());
        }

        String token = generateToken();

        Session session = new Session(user, token);

        tokenToSession.put(token, session);

        usernameToSession.put(user.getUsername(), session);

        return session;
    }

    public Session getSession(String token) {

        if (token == null || token.isBlank()) {
            return null;
        }

        return tokenToSession.get(token);
    }

    public Session getSessionByUsername(String username) {

        if (username == null || username.isBlank()) {
            return null;
        }

        return usernameToSession.get(username);
    }

    public boolean isValid(String token) {
        return getSession(token) != null;
    }

    public void removeSession(String token) {

        if (token == null || token.isBlank()) {
            return;
        }

        Session removedSession = tokenToSession.remove(token);

        if (removedSession != null) {

            //ممکن است Session جدید را هم پاک کنیم.
            usernameToSession.remove(removedSession.getUser().getUsername(), removedSession);
        }
    }

    private String generateToken() {

        byte[] bytes = new byte[32];

        random.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}