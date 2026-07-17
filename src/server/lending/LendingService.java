package server.lending;

import common.Result;
import server.book.BookService;
import server.model.Book;
import server.model.LendingRequest;
import server.model.LibraryItem;
import server.model.User;
import server.notif.NotificationService;
import server.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LendingService {

    private static final long LEND_MINUTES = 3;

    private final UserRepository userRepository;
    private final BookService bookService;
    private final NotificationService notificationService;
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger nextRequestId;
    private final ConcurrentHashMap<Integer, LendingRequest> pendingRequests;

    public LendingService(UserRepository userRepository,
                          BookService bookService,
                          NotificationService notificationService) {
        this.userRepository = userRepository;
        this.bookService = bookService;
        this.notificationService = notificationService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.nextRequestId = new AtomicInteger(1);
        this.pendingRequests = new ConcurrentHashMap<>();
    }

    public Result<LendingRequest> lendBook(User lender, int bookId, String borrowerUsername) {
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
            }
        }

        LendingRequest request = new LendingRequest(
                nextRequestId.getAndIncrement(), lender.getUsername(), borrowerUsername, bookId);
        pendingRequests.put(request.getId(), request);

        notificationService.sendToUser(
                borrowerUsername,
                lender.getUsername() + " wants to lend you book " + book.getTitle()
                        + ". Request ID: " + request.getId());
        return Result.success("Lending request sent.", request);
    }

    public Result<Void> acceptRequest(User borrower, int requestId) {
        LendingRequest request = pendingRequests.remove(requestId);
        if (request == null) {
            return Result.error("Lending request not found.");
        }
        if (borrower == null || !borrower.getUsername().equals(request.getBorrowerUsername())) {
            pendingRequests.put(requestId, request);
            return Result.error("Only borrower can accept this request.");
        }

        User lender = userRepository.find(request.getLenderUsername());
        Book book = bookService.findBookById(request.getBookId());
        if (lender == null || book == null) {
            return Result.error("Lending request is no longer valid.");
        }

        synchronized (lender) {
            synchronized (borrower) {
                LibraryItem lenderItem = lender.getLibraryItem(request.getBookId());
                if (lenderItem == null || lenderItem.isLocked()) {
                    return Result.error("Book is not available anymore.");
                }
                if (borrower.hasBook(request.getBookId())) {
                    return Result.error("Borrower already has this book.");
                }

                lenderItem.markLocked();
                borrower.addBook(book);
                LibraryItem borrowerItem = borrower.getLibraryItem(request.getBookId());
                borrowerItem.updateProgress(Math.min(1, book.getPages()), book);
            }
        }

        notificationService.sendToUser(
                borrower.getUsername(),
                "You accepted book " + book.getTitle() + " for " + LEND_MINUTES + " minutes.");
        notificationService.sendToUser(
                lender.getUsername(),
                borrower.getUsername() + " accepted your lending request for book " + book.getTitle() + ".");

        scheduler.schedule(
                () -> returnBook(lender.getUsername(), borrower.getUsername(), request.getBookId()),
                LEND_MINUTES,
                TimeUnit.MINUTES);
        return Result.success("Lending request accepted.");
    }

    public Result<Void> denyRequest(User borrower, int requestId) {
        LendingRequest request = pendingRequests.remove(requestId);
        if (request == null) {
            return Result.error("Lending request not found.");
        }
        if (borrower == null || !borrower.getUsername().equals(request.getBorrowerUsername())) {
            pendingRequests.put(requestId, request);
            return Result.error("Only borrower can deny this request.");
        }

        notificationService.sendToUser(
                request.getLenderUsername(),
                borrower.getUsername() + " denied your lending request.");
        return Result.success("Lending request denied.");
    }

    public Result<List<String>> listPendingRequests(User user) {
        if (user == null) {
            return Result.error("Unauthorized.");
        }

        List<String> result = new ArrayList<>();
        for (LendingRequest request : pendingRequests.values()) {
            if (user.getUsername().equals(request.getBorrowerUsername())) {
                result.add(request.toString());
            }
        }
        return Result.success("Pending lending requests loaded.", result);
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
