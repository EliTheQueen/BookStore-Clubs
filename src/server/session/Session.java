package server.session;

import server.model.User;

public class Session {

    private User user;

    private String token;
    private Integer viewedClubId;

    public Session(User user, String token) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be empty.");
        }

        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public Integer getViewedClubId() {
        return viewedClubId;
    }

    public void setViewedClubId(Integer viewedClubId) {
        this.viewedClubId = viewedClubId;
    }

}
