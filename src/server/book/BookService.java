package server.book;

import common.Result;
import server.model.Book;
import server.model.BookStore;
import server.model.User;

import java.util.List;

public class BookService {

    private final BookStore bookStore;

    public BookService(BookStore bookStore) {
        this.bookStore = bookStore;
    }

    public Result<List<Book>> marketList() {
        return Result.success("Books loaded successfully", bookStore.getBooks());
    }

    public List<Book> getMarketBooks() {
        return bookStore.getBooks();
    }

    public Result<Void> buyBook(User user, int bookId) {

        Book book = findBookById(bookId);

        if (book == null) {
            return Result.error("Book not found.");
        }

        synchronized (user) {

            if (user.hasBook(bookId)) {
                return Result.error("Book already purchased.");
            }

            if (!user.getWallet().hasEnough(book.getPrice())) {
                return Result.error("Insufficient balance.");
            }

            user.getWallet().withdraw(book.getPrice());

            try {
                user.addBook(book);
            } catch (RuntimeException exception) {
                //اگر بعد از کم‌شدن پول، افزودن کتاب خطا بدهد، مبلغ را برمی‌گردانیم تا عملیات نیمه‌کاره نماند. این همان Atomicity موردنیاز تمرین است.
                user.getWallet().deposit(book.getPrice());

                return Result.error("Book purchase failed: " + exception.getMessage());
            }
        }

        return Result.success("Book purchased.");
    }

    public Book findBookById(int bookId) {

        for (Book book : bookStore.getBooks()) {
            if (book.getID() == bookId) {
                return book;
            }
        }
        return null;
    }
}
