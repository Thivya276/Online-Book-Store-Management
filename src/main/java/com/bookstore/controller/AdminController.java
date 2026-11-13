package com.bookstore.controller;

import com.bookstore.model.*;
import com.bookstore.service.*;
import com.bookstore.util.LoginGuard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService admins;
    private final BookService books;
    private final UserService users;
    private final OrderService orders;
    private final ReviewService reviews;

    public AdminController(AdminService admins, BookService books, UserService users, OrderService orders, ReviewService reviews) {
        this.admins = admins; this.books = books; this.users = users; this.orders = orders; this.reviews = reviews;
    }

    private boolean notAdmin(HttpSession s) { return s.getAttribute("adminId") == null; }

    @GetMapping("/login")
    public String loginForm() { return "admin/login"; }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpServletRequest req, HttpSession s, RedirectAttributes ra) {
        String key = "admin:" + req.getRemoteAddr() + ":" + username.trim().toLowerCase();
        if (LoginGuard.isLocked(key)) {
            ra.addFlashAttribute("error", "Too many wrong attempts. Try again in " + LoginGuard.minutesLeft(key) + " minute(s).");
            return "redirect:/admin/login";
        }
        Admin a = admins.login(username, password);
        if (a == null) { LoginGuard.fail(key); ra.addFlashAttribute("error", "Wrong username or password."); return "redirect:/admin/login"; }
        LoginGuard.success(key);
        req.changeSessionId();
        s.setAttribute("adminId", a.id);
        return "redirect:/admin";
    }

    @GetMapping("/logout")
    public String logout(HttpSession s) {
        s.removeAttribute("adminId");
        return "redirect:/";
    }

    @GetMapping({"", "/"})
    public String dashboard(HttpSession s, Model m) {
        if (notAdmin(s)) return "redirect:/admin/login";
        List<Order> all = orders.all();
        double revenue = all.stream().filter(o -> !o.status.equals("Cancelled")).mapToDouble(o -> o.total).sum();
        List<Order> recent = new ArrayList<>(all);
        Collections.reverse(recent);
        m.addAttribute("defaultPw", admins.isDefaultPassword((String) s.getAttribute("adminId")));
        m.addAttribute("bookCount", books.all().size());
        m.addAttribute("userCount", users.all().size());
        m.addAttribute("orderCount", all.size());
        m.addAttribute("pending", all.stream().filter(o -> o.status.equals("Pending")).count());
        m.addAttribute("revenue", Math.round(revenue * 100) / 100.0);
        m.addAttribute("lowStock", books.all().stream().filter(b -> b.stock <= 5).toList());
        m.addAttribute("recent", recent.stream().limit(5).toList());
        return "admin/dashboard";
    }

    @GetMapping("/books")
    public String manageBooks(HttpSession s, Model m) {
        if (notAdmin(s)) return "redirect:/admin/login";
        m.addAttribute("books", books.all());
        return "admin/manage-books";
    }

    @GetMapping("/users")
    public String manageUsers(HttpSession s, Model m) {
        if (notAdmin(s)) return "redirect:/admin/login";
        m.addAttribute("users", users.all());
        return "admin/manage-users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable String id, HttpSession s, RedirectAttributes ra) {
        if (notAdmin(s)) return "redirect:/admin/login";
        users.delete(id);
        ra.addFlashAttribute("msg", "User deleted.");
        return "redirect:/admin/users";
    }

    @GetMapping("/orders")
    public String manageOrders(HttpSession s, Model m) {
        if (notAdmin(s)) return "redirect:/admin/login";
        List<Order> list = new ArrayList<>(orders.all());
        Collections.reverse(list);
        m.addAttribute("orders", list);
        m.addAttribute("statuses", OrderService.STATUSES);
        return "admin/manage-orders";
    }

    @PostMapping("/orders/{id}/status")
    public String setStatus(@PathVariable String id, @RequestParam String status, HttpSession s, RedirectAttributes ra) {
        if (notAdmin(s)) return "redirect:/admin/login";
        if (orders.setStatus(id, status)) ra.addFlashAttribute("msg", "Order " + id + " is now " + status + ".");
        else ra.addFlashAttribute("error", "That order could not be changed. Cancelled orders are final.");
        return "redirect:/admin/orders";
    }

    @GetMapping("/password")
    public String passwordForm(HttpSession s) {
        return notAdmin(s) ? "redirect:/admin/login" : "admin/change-password";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String current, @RequestParam("next") String next, @RequestParam String confirm,
                                 HttpSession s, RedirectAttributes ra) {
        if (notAdmin(s)) return "redirect:/admin/login";
        String err = admins.changePassword((String) s.getAttribute("adminId"), current, next, confirm);
        if (err != null) { ra.addFlashAttribute("error", err); return "redirect:/admin/password"; }
        ra.addFlashAttribute("msg", "Password changed.");
        return "redirect:/admin";
    }

    @GetMapping("/reviews")
    public String manageReviews(HttpSession s, Model m) {
        if (notAdmin(s)) return "redirect:/admin/login";
        List<Review> list = new ArrayList<>(reviews.all());
        Collections.reverse(list);
        m.addAttribute("reviews", list);
        return "admin/manage-reviews";
    }
}
