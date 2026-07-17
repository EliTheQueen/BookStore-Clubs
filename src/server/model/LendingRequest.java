package server.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LendingRequest implements Serializable {

    private final int id;
    private final String lenderUsername;
    private final String borrowerUsername;
    private final int bookId;
    private final LocalDateTime createdAt;

    public LendingRequest(int id, String lenderUsername, String borrowerUsername, int bookId) {
        this.id = id;
        this.lenderUsername = lenderUsername;
        this.borrowerUsername = borrowerUsername;
        this.bookId = bookId;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public String getLenderUsername() {
        return lenderUsername;
    }

    public String getBorrowerUsername() {
        return borrowerUsername;
    }

    public int getBookId() {
        return bookId;
    }

    @Override
    public String toString() {
        return "ID: " + id
                + " / Lender: " + lenderUsername
                + " / Borrower: " + borrowerUsername
                + " / Book ID: " + bookId
                + " / Created At: " + createdAt;
    }
}
