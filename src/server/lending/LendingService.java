package server.lending;

import common.Result;
import server.book.BookService;
import server.model.Book;
import server.model.LibraryItem;
import server.model.User;
import server.notif.NotificationService;
import server.repository.UserRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LendingService {

    private static final long LEND_MINUTES = 3;

    private final UserRepository userRepository;
    private final BookService bookService;
    private final NotificationService notificationService;
    private final ScheduledExecutorService scheduler;

    public LendingService(UserRepository userRepository,
                          BookService bookService,
                          NotificationService notificationService) {
        this.userRepository = userRepository;
        this.bookService = bookService;
        this.notificationService = notificationService;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public Result<Void> lendBook(User lender, int bookId, String borrowerUsername) {
        if (lender == null) {
            return Result.error("Unauthorized.");
        }
        if (borrowerUsername == null || borrowerUsername.isBlank()) {
            return Result.error("Borrower username is required.");
        }
        if (lender.getUsername().equals(borrowerUsername)) {
            return Result.error("You cannot lend a book to yourself.");
        }

        User borrower = userRepository.find(borrowerUsername);
        if (borrower == null) {
            return Result.error("Borrower not found.");
        }

        Book book = bookService.findBookById(bookId);
        if (book == null) {
            return Result.error("Book not found.");
        }

        synchronized (lender) {
            synchronized (borrower) {
                LibraryItem lenderItem = lender.getLibraryItem(bookId);
                if (lenderItem == null) {
                    return Result.error("This book is not in your library.");
                }
                if (lenderItem.isLocked()) {
                    return Result.error("This book is already lent.");
                }
                if (borrower.hasBook(bookId)) {
                    return Result.error("Borrower already has this book.");
                }

                lenderItem.markLocked();
                borrower.addBook(book);
                LibraryItem borrowerItem = borrower.getLibraryItem(bookId);
                borrowerItem.updateProgress(Math.min(1, book.getPages()), book);
            }
        }

        notificationService.sendToUser(
                borrowerUsername,
                lender.getUsername() + " lent you book " + book.getTitle() + " for " + LEND_MINUTES + " minutes.");
        notificationService.sendToUser(
                lender.getUsername(),
                "You lent book " + book.getTitle() + " to " + borrowerUsername + ".");

        scheduler.schedule(() -> returnBook(lender.getUsername(), borrowerUsername, bookId), LEND_MINUTES, TimeUnit.MINUTES);
        return Result.success("Book lent for " + LEND_MINUTES + " minutes.");
    }

    private void returnBook(String lenderUsername, String borrowerUsername, int bookId) {
        User lender = userRepository.find(lenderUsername);
        User borrower = userRepository.find(borrowerUsername);
        Book book = bookService.findBookById(bookId);

        if (lender == null || borrower == null || book == null) {
            return;
        }

        synchronized (lender) {
            synchronized (borrower) {
                borrower.removeBook(bookId);
                LibraryItem lenderItem = lender.getLibraryItem(bookId);
                if (lenderItem != null) {
                    lenderItem.markUnlocked();
                }
            }
        }

        notificationService.sendToUser(borrowerUsername, "Loan time ended for book " + book.getTitle() + ".");
        notificationService.sendToUser(lenderUsername, "Book " + book.getTitle() + " returned from " + borrowerUsername + ".");
    }
}
