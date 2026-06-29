package server.model;

import java.util.List;

public class Library {
    private List<Book> books;
    private List<Integer> bookIds;
    public enum status {
        NOT_READ,
        READING,
        READ
    }
    private status status;
    private int lastPage;

    public Book findBookById(int bookId){
        for(Book book:books){
            if (book.getID() == bookId){
                return book;
            }
        }
        return null;
    }

    public void fillBooks(){
        for(Integer bookId:bookIds){
            Book book = findBookById(bookId);
            this.books.add(book);
        }
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<Integer> getBookIds() {
        return bookIds;
    }

    public void setBookIds(List<Integer> bookIds) {
        this.bookIds = bookIds;
    }

    public status getStatus() {
        return status;
    }

    public void setStatus(status status) {
        this.status = status;
    }

    public int getLastPage() {
        return lastPage;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }
}
