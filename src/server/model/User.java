package server.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class User implements Serializable {
    private final String username;
    private final String passwordHash;
    private final Wallet wallet;
    private final Map<Integer, LibraryItem>  library;
    private final Set<Integer> clubIds;

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.wallet = new Wallet();
        this.library = new HashMap<>();
        this.clubIds = new HashSet<>();
    }

    public synchronized boolean hasBook(int bookId){
        return library.containsKey(bookId);
    }

    public synchronized void addBook(Book book){
        if (book == null) {
            //باید exception بدی که بفهمی کجا خراب نوشتی.
            throw new IllegalArgumentException("Book cannot be null");
        }

        if (hasBook(book.getID())) {
            throw new IllegalArgumentException("Book already exists");
        }
        LibraryItem libraryItem = new LibraryItem(book.getID());
        library.put(book.getID(), libraryItem);
    }

    public synchronized LibraryItem getLibraryItem(int bookId){
        return library.get(bookId);
    }

    public synchronized LibraryItem removeBook(int bookId) {
        return library.remove(bookId);
    }

    public synchronized void joinClub(int clubId){
        clubIds.add(clubId);
    }

    public synchronized void  leaveClub(int clubId){
        clubIds.remove(clubId);
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Wallet getWallet() { return wallet; }
    public Map<Integer, LibraryItem> getLibrary() { return new HashMap<>(library); }
    public Set<Integer> getClubIds() { return new HashSet<>(clubIds); }
}
