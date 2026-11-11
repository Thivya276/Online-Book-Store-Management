package com.bookstore.service;

import com.bookstore.model.Admin;
import com.bookstore.util.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AdminService {
    private static final String FILE = "admins.txt";

    private List<Admin> all() {
        List<Admin> list = new ArrayList<>();
        for (String l : FileHandler.read(FILE)) list.add(Admin.fromLine(l));
        return list;
    }

    private void saveAll(List<Admin> list) { FileHandler.write(FILE, list.stream().map(Admin::toLine).toList()); }

    /** Creates the default admin (admin / admin123) when none exists. The dashboard warns until it is changed. */
    @PostConstruct
    public void seed() {
        if (FileHandler.read(FILE).isEmpty()) {
            Admin a = new Admin();
            a.id = "A1"; a.username = "admin"; a.password = Validation.hashPassword("admin123");
            FileHandler.write(FILE, List.of(a.toLine()));
        }
    }

    public synchronized Admin login(String username, String password) {
        if (username == null || password == null) return null;
        List<Admin> list = all();
        for (Admin a : list) {
            if (a.username.equals(username.trim()) && Validation.matches(password, a.password)) {
                if (Validation.isLegacy(a.password)) { a.password = Validation.hashPassword(password); saveAll(list); }
                return a;
            }
        }
        return null;
    }

    public boolean isDefaultPassword(String id) {
        return all().stream().anyMatch(a -> a.id.equals(id) && Validation.matches("admin123", a.password));
    }

    /** Returns an error message, or null on success. */
    public synchronized String changePassword(String id, String current, String next, String confirm) {
        if (!Validation.strongPassword(next)) return "New password must be at least 8 characters with a letter and a number.";
        if (!next.equals(confirm)) return "The new passwords do not match.";
        List<Admin> list = all();
        for (Admin a : list) {
            if (a.id.equals(id)) {
                if (!Validation.matches(current, a.password)) return "Current password is wrong.";
                if (next.equals(current)) return "Choose a password different from the current one.";
                a.password = Validation.hashPassword(next);
                saveAll(list);
                return null;
            }
        }
        return "Admin account not found.";
    }
}
