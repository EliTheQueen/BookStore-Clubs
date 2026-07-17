package server.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ClubComment implements Serializable {

    private final String username;
    private final String text;
    private final LocalDateTime createdAt;

    public ClubComment(String username, String text) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Comment text is required.");
        }
        this.username = username;
        this.text = text.trim();
        this.createdAt = LocalDateTime.now();
    }

    public String toDisplayString() {
        return username + " at " + createdAt + ": " + text;
    }
}
