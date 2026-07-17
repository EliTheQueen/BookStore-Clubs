package server.database;

import server.club.ClubService;
import server.model.Club;
import server.model.User;
import server.repository.UserRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DatabaseService {

    private final String url;
    private final UserRepository userRepository;
    private final ClubService clubService;
    private final ScheduledExecutorService scheduler;
    private boolean enabled;

    public DatabaseService(String url, UserRepository userRepository, ClubService clubService) {
        this.url = url;
        this.userRepository = userRepository;
        this.clubService = clubService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.enabled = true;
    }

    public void initialize() {
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, balance REAL, book_count INTEGER)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS clubs (id INTEGER PRIMARY KEY, name TEXT, owner TEXT, member_count INTEGER)");
        } catch (SQLException exception) {
            enabled = false;
            System.out.println("JDBC database is disabled: " + exception.getMessage());
        }
    }

    public void startScheduledSave(long minutes) {
        if (!enabled) {
            return;
        }
        scheduler.scheduleAtFixedRate(this::saveSnapshot, minutes, minutes, TimeUnit.MINUTES);
    }

    public void saveSnapshot() {
        if (!enabled) {
            return;
        }

        try (Connection connection = DriverManager.getConnection(url)) {
            connection.setAutoCommit(false);
            clearTables(connection);
            saveUsers(connection);
            saveClubs(connection);
            connection.commit();
        } catch (SQLException exception) {
            System.out.println("Database snapshot failed: " + exception.getMessage());
        }
    }

    public void shutdown() {
        saveSnapshot();
        scheduler.shutdownNow();
    }

    private void clearTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM users");
            statement.executeUpdate("DELETE FROM clubs");
        }
    }

    private void saveUsers(Connection connection) throws SQLException {
        String sql = "INSERT INTO users(username, balance, book_count) VALUES(?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (User user : userRepository.findAll()) {
                statement.setString(1, user.getUsername());
                statement.setDouble(2, user.getWallet().getBalance());
                statement.setInt(3, user.getLibrary().size());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void saveClubs(Connection connection) throws SQLException {
        String sql = "INSERT INTO clubs(id, name, owner, member_count) VALUES(?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Club club : clubService.getAllClubs()) {
                statement.setInt(1, club.getId());
                statement.setString(2, club.getName());
                statement.setString(3, club.getOwnerUsername());
                statement.setInt(4, club.getMembers().size());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
