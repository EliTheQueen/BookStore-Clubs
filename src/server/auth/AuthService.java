package server.auth;

import common.Result;
import server.model.User;
import server.repository.UserRepository;
import server.session.Session;
import server.session.SessionManager;

public class AuthService {

    private final UserRepository userRepository;

    private final SessionManager sessionManager;

    public AuthService(UserRepository userRepository, SessionManager sessionManager) {

        this.userRepository = userRepository;
        this.sessionManager = sessionManager;

    }

    public Result register(String username, String passwordHash) {

        if (username == null || username.isBlank()) {
            return Result.error("Username cannot be empty.");
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            return Result.error("Password hash cannot be empty.");
        }

        User user = new User(username.trim(), passwordHash);

        boolean saved = userRepository.saveIfAbsent(user);

        if (!saved) {
            return Result.error("Username already exists.");
        }

        return Result.success("Registration successful.");
    }

    public Result<String> login(String username, String passwordHash) {

        if (username == null || username.isBlank()) {
            return Result.error("Username cannot be empty.");
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            return Result.error("Password hash cannot be empty.");
        }

        User user = userRepository.find(username.trim());

        if (user == null) {
            return Result.error("Invalid username or password.");
        }

        if (!user.getPasswordHash().equals(passwordHash)) {
            return Result.error("Invalid username or password.");
        }

        Session session = sessionManager.createSession(user);

        return Result.success("Login successful.", session.getToken());
    }

    public Result<Void> logout(String token) {

        Session session = sessionManager.getSession(token);

        if(session == null){
            return Result.error("Invalid token.");
        }

        sessionManager.removeSession(token);

        return Result.success("Logout successful.");
    }
}
