package com.bookstore.controller;

import com.bookstore.model.*;
import com.bookstore.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviews;
    private final BookService books;
    private final UserService users;

    public ReviewController(ReviewService reviews, BookService books, UserService users) {
        this.reviews = reviews; this.books = books; this.users = users;
    }

    private User current(HttpSession s) {
        String id = (String) s.getAttribute("userId");
        return id == null ? null : users.find(id);
    }

    @GetMapping("/book/{bookId}")
    public String list(@PathVariable String bookId, Model m, RedirectAttributes ra) {
        Book b = books.find(bookId);
        if (b == null) { ra.addFlashAttribute("error", "Book not found."); return "redirect:/books"; }
        m.addAttribute("book", b);
        m.addAttribute("reviews", reviews.forBook(bookId));
        return "reviews/view-reviews";
    }

    @GetMapping("/add/{bookId}")
    public String addForm(@PathVariable String bookId, HttpSession s, Model m, RedirectAttributes ra) {
        User u = current(s);
        if (u == null) { ra.addFlashAttribute("error", "Log in to write a review."); return "redirect:/login"; }
        Book b = books.find(bookId);
        if (b == null) return "redirect:/books";
        Review existing = reviews.byUserAndBook(u.id, bookId);
        if (existing != null) return "redirect:/reviews/" + existing.id + "/edit";
        m.addAttribute("book", b);
        return "reviews/add-review";
    }

    @PostMapping("/add/{bookId}")
    public String add(@PathVariable String bookId, @RequestParam String rating, @RequestParam(defaultValue = "") String comment,
                      HttpSession s, RedirectAttributes ra) {
        User u = current(s);
        if (u == null) return "redirect:/login";
        String err = reviews.add(bookId, u, rating, comment);
        if (err != null) { ra.addFlashAttribute("error", err); return "redirect:/reviews/add/" + bookId; }
        ra.addFlashAttribute("msg", "Thanks for your review.");
        return "redirect:/books/" + bookId;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, HttpSession s, Model m, RedirectAttributes ra) {
        Review r = reviews.find(id);
        if (r == null || !r.userId.equals(s.getAttribute("userId"))) { ra.addFlashAttribute("error", "You can only edit your own reviews."); return "redirect:/books"; }
        m.addAttribute("review", r);
        m.addAttribute("book", books.find(r.bookId));
        return "reviews/edit-review";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable String id, @RequestParam String rating, @RequestParam(defaultValue = "") String comment,
                       HttpSession s, RedirectAttributes ra) {
        Review r = reviews.find(id);
        if (r == null || !r.userId.equals(s.getAttribute("userId"))) return "redirect:/books";
        String err = reviews.update(id, rating, comment);
        if (err != null) { ra.addFlashAttribute("error", err); return "redirect:/reviews/" + id + "/edit"; }
        ra.addFlashAttribute("msg", "Review updated.");
        return "redirect:/books/" + r.bookId;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, HttpSession s, RedirectAttributes ra,
                         @RequestHeader(value = "Referer", required = false) String ref) {
        Review r = reviews.find(id);
        boolean admin = s.getAttribute("adminId") != null;
        if (r != null && (admin || r.userId.equals(s.getAttribute("userId")))) {
            reviews.delete(id);
            ra.addFlashAttribute("msg", "Review deleted.");
        }
        return "redirect:" + (ref == null ? "/books" : ref);
    }
}
