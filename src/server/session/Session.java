package server.session;

import server.model.User;

public class Session {

    private User user;

    private String token;

    public Session(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

}