package com.bookstore.service;

import com.bookstore.model.Book;
import com.bookstore.util.FileHandler;
import com.bookstore.util.IdGenerator;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class BookService {
    private static final String FILE = "books.txt";

    public List<Book> all() {
        List<Book> list = new ArrayList<>();
        for (String l : FileHandler.read(FILE)) list.add(Book.fromLine(l));
        return list;
    }

    private void saveAll(List<Book> list) {
        FileHandler.write(FILE, list.stream().map(Book::toLine).toList());
    }

    public Book find(String id) {
        return all().stream().filter(b -> b.id.equals(id)).findFirst().orElse(null);
    }

    public List<Book> search(String q, String genre) {
        String s = q == null ? "" : q.toLowerCase().trim();
        return all().stream()
                .filter(b -> genre == null || genre.isBlank() || b.genre.equalsIgnoreCase(genre))
                .filter(b -> s.isEmpty() || (b.title + " " + b.author + " " + b.genre).toLowerCase().contains(s))
                .toList();
    }

    public List<String> genres() {
        return all().stream().map(b -> b.genre).distinct().sorted().toList();
    }

    public synchronized void add(Book b) {
        List<Book> list = all();
        b.id = IdGenerator.next("B", list.stream().map(x -> x.id).toList());
        list.add(b);
        saveAll(list);
    }

    public synchronized void update(Book b) {
        List<Book> list = all();
        for (int i = 0; i < list.size(); i++) if (list.get(i).id.equals(b.id)) list.set(i, b);
        saveAll(list);
    }

    public synchronized void delete(String id) {
        List<Book> list = all();
        list.removeIf(b -> b.id.equals(id));
        saveAll(list);
    }

    /** Adds delta to stock. Returns false if the book is missing or stock would go below zero. */
    public synchronized boolean adjustStock(String id, int delta) {
        List<Book> list = all();
        for (Book b : list) {
            if (b.id.equals(id)) {
                if (b.stock + delta < 0) return false;
                b.stock += delta;
                saveAll(list);
                return true;
            }
        }
        return false;
    }
}
