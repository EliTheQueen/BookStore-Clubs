import server.model.User;
import server.session.Session;
import server.session.SessionManager;

public class Test {
    public static void main(String[] args) {
        User user =
                new User("ali", "hashed-password");

        SessionManager sessionManager =
                new SessionManager();

        Session session =
                sessionManager.createSession(user);

        System.out.println(
                sessionManager.getSession(
                        session.getToken()
                ).getUser().getUsername()
        );

        System.out.println(
                sessionManager.getSessionByUsername("ali")
                        .getToken()
        );

        sessionManager.removeSession(
                session.getToken()
        );

        System.out.println(
                sessionManager.getSession(
                        session.getToken()
                )
        );

        System.out.println(
                sessionManager.getSessionByUsername("ali")
        );
    }
}
