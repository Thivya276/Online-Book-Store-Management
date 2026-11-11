package com.bookstore.controller;

import com.bookstore.model.Book;
import com.bookstore.model.Review;
import com.bookstore.service.BookService;
import com.bookstore.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.*;

@Controller
public class HomeController {
    private final BookService books;
    private final ReviewService reviews;

    public HomeController(BookService books, ReviewService reviews) {
        this.books = books;
        this.reviews = reviews;
    }

    /** One genre tile on the home page: its name, how many books it has, and a colour. */
    public static class GenreTile {
        public final String name;
        public final int count, hue;
        GenreTile(String name, int count) {
            this.name = name;
            this.count = count;
            this.hue = Math.floorMod(name.hashCode(), 360);
        }
    }

    /** A highly rated review shown with the book it belongs to. */
    public static class Note {
        public final Review review;
        public final Book book;
        Note(Review review, Book book) { this.review = review; this.book = book; }
    }

    @GetMapping("/")
    public String home(Model m) {
        List<Book> all = books.all();

        List<Book> newest = new ArrayList<>(all);
        Collections.reverse(newest);

        Map<String, Integer> counts = new TreeMap<>();
        for (Book b : all) counts.merge(b.genre, 1, Integer::sum);
        List<GenreTile> tiles = counts.entrySet().stream().map(e -> new GenreTile(e.getKey(), e.getValue())).toList();

        List<Book> lastCopies = all.stream()
                .filter(b -> b.stock > 0 && b.stock <= 5)
                .sorted(Comparator.comparingInt((Book b) -> b.stock))
                .limit(4).toList();

        Map<String, Book> byId = new HashMap<>();
        for (Book b : all) byId.put(b.id, b);
        List<Review> recent = new ArrayList<>(reviews.all());
        Collections.reverse(recent);
        List<Note> notes = new ArrayList<>();
        for (Review r : recent) {
            Book b = byId.get(r.bookId);
            if (b != null && r.rating >= 4 && !r.comment.isBlank()) notes.add(new Note(r, b));
            if (notes.size() == 3) break;
        }

        m.addAttribute("home", true);                       // tells fragments.html to load home.css and home.js
        m.addAttribute("shelf", all.stream().limit(14).toList());
        m.addAttribute("featured", newest.stream().limit(6).toList());
        m.addAttribute("genreTiles", tiles);
        m.addAttribute("lastCopies", lastCopies);
        m.addAttribute("notes", notes);
        m.addAttribute("bookCount", all.size());
        m.addAttribute("inStockCount", all.stream().filter(b -> b.stock > 0).count());
        return "home";
    }
}
