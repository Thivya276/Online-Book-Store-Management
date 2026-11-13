package com.bookstore.model;

import com.bookstore.util.FileHandler;

public class Review {
    public String id, bookId, userId, userName, comment = "", date;
    public int rating;

    public static Review fromLine(String line) {
        String[] p = line.split("\\|", -1);
        Review r = new Review();
        r.id = p[0]; r.bookId = p[1]; r.userId = p[2]; r.userName = p[3];
        r.rating = Integer.parseInt(p[4]); r.comment = p[5]; r.date = p[6];
        return r;
    }

    public String toLine() {
        return String.join("|", id, bookId, userId, FileHandler.clean(userName), String.valueOf(rating),
                FileHandler.clean(comment), date);
    }
}
