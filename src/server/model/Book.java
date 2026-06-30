package server.model;

import java.io.Serializable;

public class Book implements Serializable {
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
    public String getAuthor() {
        return author;
    }
    public int getPages() {
        return pages;
    }
    public Genre getGenre() {
        return genre;
    }
    public int getYear() {
        return year;
    }
    public int getID() {
        return ID;
    }
    public double getPrice() {
        return price;
    }
    @Override
    public String toString() {
        return
                "ID: " + getID() + " / "
                + " Title: " + getTitle() + " / "
                + " Author: " + getAuthor() + " / "
                + " Price: " + getPrice() + " / "
                + " Total Pages: " + getPages() + " / "
                + " Genre: " + getGenre() + " / "
                + " Publish Year: " + getYear();
    }

    public boolean isFaust() {
        return title.equalsIgnoreCase("فاوست");
    }
}
