package server.session;

import server.model.User;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    /*
    چرا HashMap نه؟
    چون
    Server
    Multi Thread
    است.
    اگر ده Client همزمان Login کنند،
    HashMap ممکن است خراب شود.
    */
    private final ConcurrentHashMap<String, Session> sessions;

    private final SecureRandom random;

    public SessionManager() {

        sessions = new ConcurrentHashMap<>();

        random = new SecureRandom();

    }

    public Session createSession(User user) {

        String token = generateToken();

        Session session = new Session(user, token);

        sessions.put(token, session);

        return session;

    }

    public Session getSession(String token) {

        return sessions.get(token);

    }

    public void removeSession(String token) {

        sessions.remove(token);

    }

    private String generateToken() {

        //256 bit
        byte[] bytes = new byte[32];

        random.nextBytes(bytes);

        //تبدیل می‌کند به رشته.
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

    }

}