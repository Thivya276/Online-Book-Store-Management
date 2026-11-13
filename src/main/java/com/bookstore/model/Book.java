package com.bookstore.model;

import com.bookstore.util.FileHandler;

public class Book {
    public String id, title, author, genre, description = "";
    public double price;
    public int stock;

    public static Book fromLine(String line) {
        String[] p = line.split("\\|", -1);
        Book b = new Book();
        b.id = p[0]; b.title = p[1]; b.author = p[2]; b.genre = p[3];
        b.price = Double.parseDouble(p[4]); b.stock = Integer.parseInt(p[5]);
        b.description = p.length > 6 ? p[6] : "";
        return b;
    }

    public String toLine() {
        return String.join("|", id, FileHandler.clean(title), FileHandler.clean(author), FileHandler.clean(genre),
                String.valueOf(price), String.valueOf(stock), FileHandler.clean(description));
    }
}
