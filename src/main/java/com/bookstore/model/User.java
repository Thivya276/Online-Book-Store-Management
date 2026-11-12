package com.bookstore.model;

import com.bookstore.util.FileHandler;

public class User {
    public String id, name, email, password, address = "", phone = "";

    public static User fromLine(String line) {
        String[] p = line.split("\\|", -1);
        User u = new User();
        u.id = p[0]; u.name = p[1]; u.email = p[2]; u.password = p[3];
        u.address = p.length > 4 ? p[4] : ""; u.phone = p.length > 5 ? p[5] : "";
        return u;
    }

    public String toLine() {
        return String.join("|", id, FileHandler.clean(name), FileHandler.clean(email), password,
                FileHandler.clean(address), FileHandler.clean(phone));
    }
}
