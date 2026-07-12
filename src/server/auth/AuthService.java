package server.auth;

import common.Result;
import server.model.User;
import server.repository.UserRepository;
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

    public Result login(String username, String passwordHash) {

    }

}
