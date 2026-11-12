package com.bookstore.model;

import com.bookstore.util.FileHandler;
import java.util.*;

public class Order {
    public String id, userId, userName, date, status, address;
    public String paymentMethod = "Cash on delivery", paymentStatus = "Pay on delivery";
    public double total;
    public List<OrderItem> items = new ArrayList<>();

    public static Order fromLine(String line) {
        String[] p = line.split("\\|", -1);
        Order o = new Order();
        o.id = p[0]; o.userId = p[1]; o.userName = p[2]; o.date = p[3]; o.status = p[4];
        o.total = Double.parseDouble(p[5]); o.address = p[6];
        if (p.length > 7) for (String t : p[7].split(";")) if (!t.isBlank()) o.items.add(OrderItem.parse(t));
        if (p.length > 9) { o.paymentMethod = p[8]; o.paymentStatus = p[9]; }
        return o;
    }

    public String toLine() {
        StringBuilder sb = new StringBuilder();
        for (OrderItem i : items) { if (sb.length() > 0) sb.append(';'); sb.append(i.token()); }
        return String.join("|", id, userId, FileHandler.clean(userName), date, status,
                String.valueOf(total), FileHandler.clean(address), sb.toString(),
                FileHandler.clean(paymentMethod), FileHandler.clean(paymentStatus));
    }
}
