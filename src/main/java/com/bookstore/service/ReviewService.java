package com.bookstore.service;

import com.bookstore.model.*;
import com.bookstore.util.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class ReviewService {
    private static final String FILE = "reviews.txt";

    public List<Review> all() {
        List<Review> list = new ArrayList<>();
        for (String l : FileHandler.read(FILE)) list.add(Review.fromLine(l));
        return list;
    }

    private void saveAll(List<Review> list) {
        FileHandler.write(FILE, list.stream().map(Review::toLine).toList());
    }

    public Review find(String id) {
        return all().stream().filter(r -> r.id.equals(id)).findFirst().orElse(null);
    }

    /** Newest first. */
    public List<Review> forBook(String bookId) {
        List<Review> list = new ArrayList<>(all().stream().filter(r -> r.bookId.equals(bookId)).toList());
        Collections.reverse(list);
        return list;
    }

    public Review byUserAndBook(String userId, String bookId) {
        return all().stream().filter(r -> r.userId.equals(userId) && r.bookId.equals(bookId)).findFirst().orElse(null);
    }

    public double average(List<Review> list) {
        return list.stream().mapToInt(r -> r.rating).average().orElse(0);
    }

    /** Returns an error message, or null on success. */
    public synchronized String add(String bookId, User user, String rating, String comment) {
        Integer r = Validation.integer(rating);
        if (r == null || r < 1 || r > 5) return "Choose a rating from 1 to 5.";
        if (byUserAndBook(user.id, bookId) != null) return "You have already reviewed this book. Edit your review instead.";
        List<Review> list = all();
        Review rv = new Review();
        rv.id = IdGenerator.next("R", list.stream().map(x -> x.id).toList());
        rv.bookId = bookId; rv.userId = user.id; rv.userName = user.name;
        rv.rating = r; rv.comment = FileHandler.clean(comment); rv.date = LocalDate.now().toString();
        list.add(rv);
        saveAll(list);
        return null;
    }

    public synchronized String update(String id, String rating, String comment) {
        Integer r = Validation.integer(rating);
        if (r == null || r < 1 || r > 5) return "Choose a rating from 1 to 5.";
        List<Review> list = all();
        for (Review rv : list) if (rv.id.equals(id)) { rv.rating = r; rv.comment = FileHandler.clean(comment); }
        saveAll(list);
        return null;
    }

    public synchronized void delete(String id) {
        List<Review> list = all();
        list.removeIf(r -> r.id.equals(id));
        saveAll(list);
    }

    public synchronized void deleteForBook(String bookId) {
        List<Review> list = all();
        list.removeIf(r -> r.bookId.equals(bookId));
        saveAll(list);
    }
}
