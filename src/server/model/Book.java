package server.model;

public class Book {
    private String title;
    private String author;
    private int pages;
    public enum  Genre {
        رمان,
        علمی_تخیلی_و_فانتزی,
        نمایشنامه,
        شعر,
        فلسفه,
        روان‌شناسی,
        تاریخ,
        سورس_دانشگاهی
    }
    private Genre genre;
    private int year;
    private int ID;
    private double price;

    public Book(String title, String author, int pages, Genre genre, int year, int ID, double price) {
        this.title = title;
        this.author = author;
        this.pages = pages;
        this.genre = genre;
        this.year = year;
        this.ID = ID;
        this.price = price;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public int getPages() {
        return pages;
    }
    public void setPages(int pages) {
        this.pages = pages;
    }
    public Genre getGenre() {
        return genre;
    }
    public void setGenre(Genre genre) {
        this.genre = genre;
    }
    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public int getID() {
        return ID;
    }
    public void setID(int ID) {
        this.ID = ID;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    @Override
    public String toString() {
        return
                "ID: " + getID() + "/"
                + " Title: " + getTitle() + "/"
                + " Author: " + getAuthor() + "/"
                + " Price: " + getPrice() + "/"
                + " Total Pages: " + getPages() + "/"
                + " Genre: " + getGenre() + "/"
                + " Publish Year: " + getYear();
    }
}
