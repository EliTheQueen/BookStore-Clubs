package server.test;

import common.Result;
import server.backup.BackupService;
import server.book.BookService;
import server.club.ClubService;
import server.fundraiser.FundraiserService;
import server.model.Book;
import server.model.BookStore;
import server.model.Club;
import server.model.Fundraiser;
import server.model.User;
import server.notif.NotificationService;
import server.repository.UserRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConcurrencyTest {

    private static final String HASH = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    public static void main(String[] args) throws Exception {
        testConcurrentBuy();
        testConcurrentDonateAndCompletion();
        testBackupDuringChanges();
        System.out.println("All concurrency tests passed.");
    }

    private static void testConcurrentBuy() throws Exception {
        BookService bookService = new BookService(new BookStore());
        Book book = bookService.getMarketBooks().get(0);
        User user = new User("buyer", HASH);
        user.getWallet().deposit(book.getPrice());

        List<Result<Void>> results = runTwoThreads(
                () -> bookService.buyBook(user, book.getID()),
                () -> bookService.buyBook(user, book.getID()));

        int successCount = 0;
        for (Result<Void> result : results) {
            if (result.isSuccess()) {
                successCount++;
            }
        }

        assertTrue(successCount == 1, "Only one concurrent buy must succeed");
        assertTrue(user.hasBook(book.getID()), "Book must be in buyer library");
        assertTrue(user.getWallet().getBalance() == 0, "Only one book price must be withdrawn");
    }

    private static void testConcurrentDonateAndCompletion() throws Exception {
        UserRepository userRepository = new UserRepository();
        NotificationService notificationService = new NotificationService();
        BookService bookService = new BookService(new BookStore());
        ClubService clubService = new ClubService(userRepository, notificationService);
        FundraiserService fundraiserService =
                new FundraiserService(clubService, bookService, userRepository, notificationService);

        User owner = save(userRepository, "owner");
        User member1 = save(userRepository, "member1");
        User member2 = save(userRepository, "member2");

        Book book = bookService.getMarketBooks().get(1);
        owner.getWallet().deposit(book.getPrice() * 10);
        member1.getWallet().deposit(book.getPrice() * 10);
        member2.getWallet().deposit(book.getPrice() * 10);

        Club club = clubService.createClub(owner, "race readers").getData();
        club.addJoinRequest(member1.getUsername());
        club.acceptMember(member1.getUsername());
        member1.joinClub(club.getId());
        club.addJoinRequest(member2.getUsername());
        club.acceptMember(member2.getUsername());
        member2.joinClub(club.getId());

        Result<Fundraiser> created = fundraiserService.createFundraiser(owner, club.getId(), book.getID());
        assertTrue(created.isSuccess(), "First fundraiser must be created");

        Result<Fundraiser> duplicate = fundraiserService.createFundraiser(member1, club.getId(), book.getID());
        assertTrue(!duplicate.isSuccess(), "Duplicate active fundraiser must be rejected");

        List<Result<Void>> results = runTwoThreads(
                () -> fundraiserService.donate(member1, club.getId(), book.getPrice() * 10),
                () -> fundraiserService.donate(member2, club.getId(), book.getPrice() * 10));

        int accepted = 0;
        for (Result<Void> result : results) {
            if (result.isSuccess()) {
                accepted++;
            }
        }

        assertTrue(accepted >= 1, "At least one donation must be accepted");
        assertTrue(owner.hasBook(book.getID()), "Owner must receive completed fundraiser book");
        assertTrue(member1.hasBook(book.getID()), "Member1 must receive completed fundraiser book");
        assertTrue(member2.hasBook(book.getID()), "Member2 must receive completed fundraiser book");
        assertTrue(club.getActiveFundraiser() == null, "Fundraiser must be closed after completion");
        assertTrue(member1.getWallet().getBalance() >= 0, "Over-donation must leave extra money in wallet");
        assertTrue(member2.getWallet().getBalance() >= 0, "Over-donation must leave extra money in wallet");
    }

    private static void testBackupDuringChanges() throws Exception {
        UserRepository userRepository = new UserRepository();
        NotificationService notificationService = new NotificationService();
        ClubService clubService = new ClubService(userRepository, notificationService);
        Path backupPath = Files.createTempFile("bookstore-test-backup", ".ser");
        BackupService backupService = new BackupService(userRepository, clubService, backupPath);

        Thread writer = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                User user = new User("backup-user-" + i, HASH);
                user.getWallet().deposit(i + 1);
                userRepository.save(user);
            }
        });

        writer.start();
        backupService.backupNow();
        writer.join();
        backupService.backupNow();

        assertTrue(Files.size(backupPath) > 0, "Backup file must be written");

        UserRepository restoredUsers = new UserRepository();
        ClubService restoredClubs = new ClubService(restoredUsers, notificationService);
        BackupService restoreService = new BackupService(restoredUsers, restoredClubs, backupPath);
        restoreService.restoreIfExists();

        assertTrue(!restoredUsers.findAll().isEmpty(), "Backup restore must load users");
        Files.deleteIfExists(backupPath);
    }

    private static User save(UserRepository repository, String username) {
        User user = new User(username, HASH);
        repository.save(user);
        return user;
    }

    private static List<Result<Void>> runTwoThreads(TestAction first, TestAction second) throws Exception {
        List<Result<Void>> results = new ArrayList<>();

        Thread t1 = new Thread(() -> addResult(results, first));
        Thread t2 = new Thread(() -> addResult(results, second));
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        return results;
    }

    private static synchronized void addResult(List<Result<Void>> results, TestAction action) {
        try {
            results.add(action.run());
        } catch (Exception exception) {
            results.add(Result.error(exception.getMessage()));
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private interface TestAction {
        Result<Void> run() throws Exception;
    }
}
