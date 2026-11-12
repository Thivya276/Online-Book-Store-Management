package com.bookstore.service;

import com.bookstore.model.*;
import com.bookstore.util.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class OrderService {
    public static final List<String> STATUSES = List.of("Pending", "Processing", "Shipped", "Delivered", "Cancelled");
    private static final String FILE = "orders.txt";
    private final BookService books;

    private final PaymentService payments;

    public OrderService(BookService books, PaymentService payments) { this.books = books; this.payments = payments; }

    public List<Order> all() {
        List<Order> list = new ArrayList<>();
        for (String l : FileHandler.read(FILE)) list.add(Order.fromLine(l));
        return list;
    }

    private void saveAll(List<Order> list) {
        FileHandler.write(FILE, list.stream().map(Order::toLine).toList());
    }

    public Order find(String id) {
        return all().stream().filter(o -> o.id.equals(id)).findFirst().orElse(null);
    }

    public List<Order> forUser(String userId) {
        List<Order> list = new ArrayList<>(all().stream().filter(o -> o.userId.equals(userId)).toList());
        Collections.reverse(list);
        return list;
    }

    /** Checks stock, reduces it, and saves the order. Throws IllegalArgumentException with a user-facing message. */
    public synchronized Order place(User user, List<OrderItem> items, String address, String paymentMethod) {
        if (items.isEmpty()) throw new IllegalArgumentException("Your cart is empty.");
        if (!Validation.notBlank(address)) throw new IllegalArgumentException("A shipping address is required.");
        for (OrderItem i : items) {
            Book b = books.find(i.bookId);
            if (b == null || b.stock < i.qty)
                throw new IllegalArgumentException("Not enough stock for \"" + i.title + "\". Lower the quantity and try again.");
        }
        items.forEach(i -> books.adjustStock(i.bookId, -i.qty));
        List<Order> list = all();
        Order o = new Order();
        o.id = IdGenerator.next("O", list.stream().map(x -> x.id).toList());
        o.userId = user.id; o.userName = user.name; o.date = LocalDate.now().toString();
        o.status = "Pending"; o.address = address.trim(); o.items = items;
        o.paymentMethod = payments.label(paymentMethod); o.paymentStatus = payments.statusFor(paymentMethod);
        o.total = Math.round(items.stream().mapToDouble(OrderItem::subtotal).sum() * 100) / 100.0;
        list.add(o);
        saveAll(list);
        return o;
    }

    /** Cancelled orders are final. Cancelling puts the copies back in stock. */
    public synchronized boolean setStatus(String id, String status) {
        if (!STATUSES.contains(status)) return false;
        List<Order> list = all();
        for (Order o : list) {
            if (o.id.equals(id)) {
                if (o.status.equals("Cancelled")) return false;
                if (status.equals("Cancelled")) o.items.forEach(i -> books.adjustStock(i.bookId, i.qty));
                o.status = status;
                if (status.equals("Delivered") && o.paymentStatus.equals("Pay on delivery")) o.paymentStatus = "Paid";
                if (status.equals("Cancelled")) o.paymentStatus = o.paymentStatus.startsWith("Paid") ? "Refund due" : "Not charged";
                saveAll(list);
                return true;
            }
        }
        return false;
    }
}
