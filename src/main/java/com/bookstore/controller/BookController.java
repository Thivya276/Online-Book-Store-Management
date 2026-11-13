package com.bookstore.controller;

import com.bookstore.model.*;
import com.bookstore.service.*;
import com.bookstore.util.Validation;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {
    private final BookService books;
    private final ReviewService reviews;

    public BookController(BookService books, ReviewService reviews) { this.books = books; this.reviews = reviews; }

    private boolean notAdmin(HttpSession s) { return s.getAttribute("adminId") == null; }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "") String genre, Model m) {
        m.addAttribute("books", books.search(q, genre));
        m.addAttribute("genres", books.genres());
        m.addAttribute("q", q);
        m.addAttribute("genre", genre);
        return "books/book-list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable String id, Model m, RedirectAttributes ra) {
        Book b = books.find(id);
        if (b == null) { ra.addFlashAttribute("error", "That book no longer exists."); return "redirect:/books"; }
        List<Review> all = reviews.forBook(id);
        double avg = reviews.average(all);
        m.addAttribute("book", b);
        m.addAttribute("reviews", all.stream().limit(3).toList());
        m.addAttribute("count", all.size());
        m.addAttribute("avg", avg);
        m.addAttribute("avgRounded", (int) Math.round(avg));
        return "books/book-details";
    }

    @GetMapping("/add")
    public String addForm(HttpSession s) {
        return notAdmin(s) ? "redirect:/admin/login" : "books/add-book";
    }

    @PostMapping("/add")
    public String add(@RequestParam String title, @RequestParam String author, @RequestParam String genre,
                      @RequestParam String price, @RequestParam String stock, @RequestParam(defaultValue = "") String description,
                      HttpSession s, RedirectAttributes ra) {
        if (notAdmin(s)) return "redirect:/admin/login";
        String err = check(title, author, genre, price, stock);
        if (err != null) { ra.addFlashAttribute("error", err); return "redirect:/books/add"; }
        books.add(build(null, title, author, genre, price, stock, description));
        ra.addFlashAttribute("msg", "Book added.");
        return "redirect:/admin/books";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, HttpSession s, Model m, RedirectAttributes ra) {
        if (notAdmin(s)) return "redirect:/admin/login";
        Book b = books.find(id);
        if (b == null) { ra.addFlashAttribute("error", "Book not found."); return "redirect:/admin/books"; }
        m.addAttribute("book", b);
        return "books/edit-book";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable String id, @RequestParam String title, @RequestParam String author, @RequestParam String genre,
                       @RequestParam String price, @RequestParam String stock, @RequestParam(defaultValue = "") String description,
                       HttpSession s, RedirectAttributes ra) {
        if (notAdmin(s)) return "redirect:/admin/login";
        String err = check(title, author, genre, price, stock);
        if (err != null) { ra.addFlashAttribute("error", err); return "redirect:/books/" + id + "/edit"; }
        books.update(build(id, title, author, genre, price, stock, description));
        ra.addFlashAttribute("msg", "Book updated.");
        return "redirect:/admin/books";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, HttpSession s, RedirectAttributes ra) {
        if (notAdmin(s)) return "redirect:/admin/login";
        books.delete(id);
        reviews.deleteForBook(id);
        ra.addFlashAttribute("msg", "Book deleted.");
        return "redirect:/admin/books";
    }

    private String check(String title, String author, String genre, String price, String stock) {
        if (!Validation.notBlank(title) || !Validation.notBlank(author) || !Validation.notBlank(genre))
            return "Title, author and genre are required.";
        if (Validation.price(price) == null) return "Price must be a number of 0 or more.";
        if (Validation.integer(stock) == null) return "Stock must be a whole number of 0 or more.";
        return null;
    }

    private Book build(String id, String title, String author, String genre, String price, String stock, String description) {
        Book b = new Book();
        b.id = id; b.title = title.trim(); b.author = author.trim(); b.genre = genre.trim();
        b.price = Validation.price(price); b.stock = Validation.integer(stock); b.description = description;
        return b;
    }
}
