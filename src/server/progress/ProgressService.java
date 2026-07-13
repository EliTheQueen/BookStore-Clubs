package server.progress;

import common.Result;
import server.model.Book;
import server.model.BookLibraryStatus;
import server.model.BookStore;
import server.model.LibraryItem;
import server.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProgressService {

    private final BookStore bookStore;

    public ProgressService(BookStore bookStore) {
        this.bookStore = bookStore;
    }

    public Result<Void> submitProgress(User user, int bookId, int page) {

        if (user == null) {
            return Result.error("Unauthorized.");
        }

        LibraryItem item = user.getLibraryItem(bookId);

        if (item == null) {
            return Result.error("This book is not in your library.");
        }

        Book book = findBook(bookId);

        if (book == null) {
            return Result.error("Book not found.");
        }

        try {

            synchronized (user) {
                item.updateProgress(page, book);
            }

            return Result.success("Progress updated successfully.");

        } catch (IllegalArgumentException | IllegalStateException exception) {

            return Result.error(exception.getMessage());
        }
    }

    public Result<List<String>> listNotRead(User user) {
        return listByStatus(user, BookLibraryStatus.BookStatus.NOT_READ);
    }

    public Result<List<String>> listReading(User user) {
        return listByStatus(user, BookLibraryStatus.BookStatus.READING);
    }

    public Result<List<String>> listRead(User user) {
        return listByStatus(user, BookLibraryStatus.BookStatus.READ);
    }

    private Result<List<String>> listByStatus(User user, BookLibraryStatus.BookStatus status) {

        if (user == null) {
            return Result.error("Unauthorized.");
        }

        List<String> result = new ArrayList<>();

        for (Map.Entry<Integer, LibraryItem> entry : user.getLibrary().entrySet()) {

            LibraryItem item = entry.getValue();

            if (item.getBookStatus() != status) {
                continue;
            }

            Book book = findBook(entry.getKey());

            if (book == null) {
                continue;
            }

            result.add(
                    "ID: " + book.getID() +
                    " / Title: " + book.getTitle() +
                    " / Last Page: " + item.getLastPage());
        }

        return Result.success("Library loaded successfully.", result);
    }

    private Book findBook(int bookId) {

        for (Book book : bookStore.getBooks()) {

            if (book.getID() == bookId) {
                return book;
            }
        }

        return null;
    }
}