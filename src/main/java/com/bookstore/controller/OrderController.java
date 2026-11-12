package com.bookstore.controller;

import com.bookstore.model.*;
import com.bookstore.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
public class OrderController {
    private final OrderService orders;
    private final BookService books;
    private final UserService users;

    public OrderController(OrderService orders, BookService books, UserService users) {
        this.orders = orders; this.books = books; this.users = users;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> cart(HttpSession s) {
        Object c = s.getAttribute("cart");
        if (c == null) { c = new LinkedHashMap<String, Integer>(); s.setAttribute("cart", c); }
        return (Map<String, Integer>) c;
    }

    private List<OrderItem> lines(HttpSession s) {
        List<OrderItem> out = new ArrayList<>();
        for (Map.Entry<String, Integer> e : cart(s).entrySet()) {
            Book b = books.find(e.getKey());
            if (b == null) continue;
            OrderItem i = new OrderItem();
            i.bookId = b.id; i.title = b.title; i.price = b.price; i.qty = e.getValue();
            out.add(i);
        }
        return out;
    }

    private boolean canView(Order o, HttpSession s) {
        return s.getAttribute("adminId") != null || o.userId.equals(s.getAttribute("userId"));
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam String bookId, @RequestParam(defaultValue = "1") int qty,
                            @RequestHeader(value = "Referer", required = false) String ref, HttpSession s, RedirectAttributes ra) {
        Book b = books.find(bookId);
        if (b == null || b.stock == 0) { ra.addFlashAttribute("error", "That book is out of stock."); return "redirect:/books"; }
        Map<String, Integer> c = cart(s);
        c.put(bookId, Math.min(b.stock, c.getOrDefault(bookId, 0) + Math.max(1, qty)));
        ra.addFlashAttribute("msg", "Added \"" + b.title + "\" to your cart.");
        return "redirect:" + (ref == null ? "/books" : ref);
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam String bookId, HttpSession s) {
        cart(s).remove(bookId);
        return "redirect:/orders/new";
    }

    @GetMapping("/orders/new")
    public String cartPage(HttpSession s, Model m) {
        List<OrderItem> items = lines(s);
        m.addAttribute("items", items);
        m.addAttribute("total", Math.round(items.stream().mapToDouble(OrderItem::subtotal).sum() * 100) / 100.0);
        String uid = (String) s.getAttribute("userId");
        m.addAttribute("address", uid == null ? "" : users.find(uid).address);
        return "orders/place-order";
    }

    @PostMapping("/orders/place")
    public String place(@RequestParam String address, @RequestParam(defaultValue = "COD") String paymentMethod, HttpSession s, RedirectAttributes ra) {
        String uid = (String) s.getAttribute("userId");
        User u = uid == null ? null : users.find(uid);
        if (u == null) { ra.addFlashAttribute("error", "Log in to place your order."); return "redirect:/login"; }
        try {
            Order o = orders.place(u, lines(s), address, paymentMethod);
            cart(s).clear();
            return "redirect:/orders/" + o.id + "/confirmation";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/new";
        }
    }

    @GetMapping("/orders/{id}/confirmation")
    public String confirmation(@PathVariable String id, HttpSession s, Model m) {
        Order o = orders.find(id);
        if (o == null || !canView(o, s)) return "redirect:/";
        m.addAttribute("order", o);
        return "orders/order-confirmation";
    }

    @GetMapping("/orders")
    public String mine(HttpSession s, Model m, RedirectAttributes ra) {
        String uid = (String) s.getAttribute("userId");
        if (uid == null) { ra.addFlashAttribute("error", "Log in to see your orders."); return "redirect:/login"; }
        m.addAttribute("orders", orders.forUser(uid));
        return "orders/my-orders";
    }

    @GetMapping("/orders/{id}")
    public String details(@PathVariable String id, HttpSession s, Model m, RedirectAttributes ra) {
        Order o = orders.find(id);
        if (o == null || !canView(o, s)) { ra.addFlashAttribute("error", "Order not found."); return "redirect:/orders"; }
        m.addAttribute("order", o);
        return "orders/order-details";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable String id, HttpSession s, RedirectAttributes ra) {
        Order o = orders.find(id);
        if (o == null || !o.userId.equals(s.getAttribute("userId"))) return "redirect:/orders";
        if (!o.status.equals("Pending")) {
            ra.addFlashAttribute("error", "Only pending orders can be cancelled.");
        } else {
            orders.setStatus(id, "Cancelled");
            ra.addFlashAttribute("msg", "Order cancelled. The books are back in stock.");
        }
        return "redirect:/orders/" + id;
    }
}
