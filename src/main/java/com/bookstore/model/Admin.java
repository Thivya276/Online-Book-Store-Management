package com.bookstore.model;

public class Admin {
    public String id, username, password;

    public static Admin fromLine(String line) {
        String[] p = line.split("\\|", -1);
        Admin a = new Admin();
        a.id = p[0]; a.username = p[1]; a.password = p[2];
        return a;
    }

    public String toLine() { return String.join("|", id, username, password); }
}
