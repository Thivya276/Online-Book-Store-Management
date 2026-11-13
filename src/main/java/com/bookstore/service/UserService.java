package com.bookstore.service;

import com.bookstore.model.User;
import com.bookstore.util.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService {
    private static final String FILE = "users.txt";

    public List<User> all() {
        List<User> list = new ArrayList<>();
        for (String l : FileHandler.read(FILE)) list.add(User.fromLine(l));
        return list;
    }

    private void saveAll(List<User> list) {
        FileHandler.write(FILE, list.stream().map(User::toLine).toList());
    }

    public User find(String id) {
        return all().stream().filter(u -> u.id.equals(id)).findFirst().orElse(null);
    }

    /** Returns an error message, or null on success. */
    public synchronized String register(String name, String email, String password, String address, String phone) {
        if (!Validation.notBlank(name)) return "Name is required.";
        if (!Validation.email(email)) return "Enter a valid email address.";
        if (!Validation.password(password)) return "Password must be at least 6 characters.";
        List<User> list = all();
        if (list.stream().anyMatch(u -> u.email.equalsIgnoreCase(email.trim()))) return "That email is already registered.";
        User u = new User();
        u.id = IdGenerator.next("U", list.stream().map(x -> x.id).toList());
        u.name = name.trim(); u.email = email.trim(); u.password = Validation.hashPassword(password);
        u.address = address == null ? "" : address; u.phone = phone == null ? "" : phone;
        list.add(u);
        saveAll(list);
        return null;
    }

    public synchronized User login(String email, String password) {
        if (email == null || password == null) return null;
        List<User> list = all();
        for (User u : list) {
            if (u.email.equalsIgnoreCase(email.trim()) && Validation.matches(password, u.password)) {
                if (Validation.isLegacy(u.password)) { u.password = Validation.hashPassword(password); saveAll(list); }
                return u;
            }
        }
        return null;
    }

    /** Returns an error message, or null on success. */
    public synchronized String updateProfile(String id, String name, String address, String phone) {
        if (!Validation.notBlank(name)) return "Name is required.";
        List<User> list = all();
        for (User u : list) {
            if (u.id.equals(id)) { u.name = name.trim(); u.address = address; u.phone = phone; }
        }
        saveAll(list);
        return null;
    }

    public synchronized void delete(String id) {
        List<User> list = all();
        list.removeIf(u -> u.id.equals(id));
        saveAll(list);
    }
}
