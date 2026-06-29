package server.model;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BookStore {

    private List<Book> books;

    public BookStore() {
        books = new ArrayList<>();
        loadBooksFromJSON();
    }

    private void loadBooksFromJSON() {
        try {
            String json = Files.readString(Paths.get("books.json"));

            json = json.substring(1, json.length() - 1);
            String[] items = json.split("\\},\\s*\\{");

            for (String item : items) {
                item = item.replace("{", "").replace("}", "");

                String title = getString(item, "title");
                String author = getString(item, "author");
                int pages = getInt(item, "totalPages");
                Book.Genre genre = convertGenre(getString(item, "genre"));
                int year = getInt(item, "publishYear");
                int id = Integer.parseInt(getString(item, "id").replace("b", ""));
                double price = getDouble(item, "price");

                books.add(new Book(title, author, pages, genre, year, id, price));

            }
        } catch (Exception e) {
            System.out.println("Error while reading books from json file: " + e.getMessage());
        }
    }

    private String getString(String item, String key) {
        String pattern = "\"" + key + "\":";
        int start = item.indexOf(pattern) + pattern.length();

        while (item.charAt(start) == ' ') start++;

        if (item.charAt(start) == '"') {
            start++;
            int end = item.indexOf("\"", start);
            return item.substring(start, end);
        }

        int end = item.indexOf(",", start);
        if (end == -1) end = item.length();

        return item.substring(start, end).trim();
    }

    private int getInt(String item, String key) {
        return Integer.parseInt(getString(item, key));
    }

    private double getDouble(String item, String key) {
        return Double.parseDouble(getString(item, key));
    }

    private Book.Genre convertGenre(String genre) {
        switch (genre) {
            case "رمان":
                return Book.Genre.رمان;
            case "علمی‌-تخیلی و فانتزی":
                return Book.Genre.علمی_تخیلی_و_فانتزی;
            case "نمایشنامه":
                return Book.Genre.نمایشنامه;
            case "شعر":
                return Book.Genre.شعر;
            case "فلسفه":
                return Book.Genre.فلسفه;
            case "روان‌شناسی":
                return Book.Genre.روان‌شناسی;
            case "تاریخ":
                return Book.Genre.تاریخ;
            case "سورس دانشگاهی":
                return Book.Genre.سورس_دانشگاهی;
            default:
                throw new IllegalArgumentException("Unknown genre: " + genre);
        }
    }

    public List<Book> getBooks() {
        return books;
    }
}
