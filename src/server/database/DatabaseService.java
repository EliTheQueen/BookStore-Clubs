package server.database;

import server.club.ClubService;
import server.book.BookService;
import server.model.Book;
import server.model.Club;
import server.model.Fundraiser;
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
    private final BookService bookService;
    private final ScheduledExecutorService scheduler;
    private boolean enabled;

    public DatabaseService(String url,
                           UserRepository userRepository,
                           ClubService clubService,
                           BookService bookService) {
        this.url = url;
        this.userRepository = userRepository;
        this.clubService = clubService;
        this.bookService = bookService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.enabled = true;
    }

    public void initialize() {
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, balance REAL, book_count INTEGER)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY, title TEXT, author TEXT, pages INTEGER, genre TEXT, publish_year INTEGER, price REAL)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS clubs (id INTEGER PRIMARY KEY, name TEXT, owner TEXT, member_count INTEGER)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS fundraisers (id INTEGER PRIMARY KEY, club_id INTEGER, book_id INTEGER, target_amount REAL, current_amount REAL, status TEXT)");
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
            saveBooks(connection);
            saveUsers(connection);
            saveClubs(connection);
            saveFundraisers(connection);
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
            statement.executeUpdate("DELETE FROM books");
            statement.executeUpdate("DELETE FROM clubs");
            statement.executeUpdate("DELETE FROM fundraisers");
        }
    }

    private void saveBooks(Connection connection) throws SQLException {
        String sql = "INSERT INTO books(id, title, author, pages, genre, publish_year, price) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Book book : bookService.getMarketBooks()) {
                statement.setInt(1, book.getID());
                statement.setString(2, book.getTitle());
                statement.setString(3, book.getAuthor());
                statement.setInt(4, book.getPages());
                statement.setString(5, book.getGenre().toString());
                statement.setInt(6, book.getYear());
                statement.setDouble(7, book.getPrice());
                statement.addBatch();
            }
            statement.executeBatch();
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

    private void saveFundraisers(Connection connection) throws SQLException {
        String sql = "INSERT INTO fundraisers(id, club_id, book_id, target_amount, current_amount, status) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Club club : clubService.getAllClubs()) {
                Fundraiser fundraiser = club.getActiveFundraiser();
                if (fundraiser == null) {
                    continue;
                }
                statement.setInt(1, fundraiser.getId());
                statement.setInt(2, fundraiser.getClubId());
                statement.setInt(3, fundraiser.getBookId());
                statement.setDouble(4, fundraiser.getTargetAmount());
                statement.setDouble(5, fundraiser.getCurrentAmount());
                statement.setString(6, fundraiser.getStatus().toString());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
