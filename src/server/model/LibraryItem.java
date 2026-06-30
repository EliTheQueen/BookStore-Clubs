package server.model;

import java.io.Serializable;

public class LibraryItem implements Serializable {

    private int bookId;
    private BookLibraryStatus.BookStatus bookStatus;
    private int lastPage;
    private boolean locked;

    public LibraryItem(int bookId) {
        this.bookId = bookId;
        this.bookStatus = BookLibraryStatus.BookStatus.NOT_READ;
        this.lastPage = 0;
        this.locked = false;
    }

    public void updateProgress(int page, Book book) {
        if (locked) {
            throw new IllegalStateException("This book is currently locked");
        }

        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        if (book.getID() != this.bookId) {
            throw new IllegalArgumentException("Book does not match this library item");
        }

        if (page < 0 || page > book.getPages()) {
            throw new IllegalArgumentException("Invalid page number");
        }

        if (book.isFaust() && page == book.getPages()) {
            throw new IllegalStateException("Faust can never be marked as read");
        }

        this.lastPage = page;

        if (page == 0) {
            this.bookStatus = BookLibraryStatus.BookStatus.NOT_READ;
        } else if (page < book.getPages()) {
            this.bookStatus = BookLibraryStatus.BookStatus.READING;
        } else {
            this.bookStatus = BookLibraryStatus.BookStatus.READ;
        }
    }

    public void markLocked() {
        this.locked = true;
    }

    public void markUnlocked() {
        this.locked = false;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getLastPage() {
        return lastPage;
    }

    public BookLibraryStatus.BookStatus getBookStatus() {
        return bookStatus;
    }

    public int getBookId() {
        return bookId;
    }
}