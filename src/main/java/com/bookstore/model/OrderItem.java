package com.bookstore.model;

import com.bookstore.util.FileHandler;

public class OrderItem {
    public String bookId, title;
    public int qty;
    public double price;

    public double subtotal() { return Math.round(qty * price * 100) / 100.0; }

    /** Stored as bookId~title~qty~price inside the order line. */
    public String token() {
        return String.join("~", bookId, FileHandler.clean(title).replace("~", " ").replace(";", " "),
                String.valueOf(qty), String.valueOf(price));
    }

    public static OrderItem parse(String t) {
        String[] p = t.split("~", -1);
        OrderItem i = new OrderItem();
        i.bookId = p[0]; i.title = p[1]; i.qty = Integer.parseInt(p[2]); i.price = Double.parseDouble(p[3]);
        return i;
    }
}
