package com.bookstore.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Validation {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static String hashPassword(String raw) { return ENCODER.encode(raw); }

    /** Accepts BCrypt hashes and the older SHA-256 hashes, so existing accounts keep working. */
    public static boolean matches(String raw, String stored) {
        if (raw == null || stored == null) return false;
        return stored.startsWith("$2") ? ENCODER.matches(raw, stored) : hash(raw).equals(stored);
    }

    public static boolean isLegacy(String stored) { return !stored.startsWith("$2"); }

    public static boolean strongPassword(String s) {
        return s != null && s.length() >= 8 && s.matches(".*[A-Za-z].*") && s.matches(".*\\d.*");
    }

    public static boolean notBlank(String s) { return s != null && !s.isBlank(); }

    public static boolean email(String s) {
        return s != null && s.trim().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    }

    public static boolean password(String s) { return s != null && s.length() >= 6; }

    /** Parses a price of 0 or more; returns null when invalid. */
    public static Double price(String s) {
        try { double d = Double.parseDouble(s.trim()); return d >= 0 ? d : null; }
        catch (Exception e) { return null; }
    }

    /** Parses a whole number of 0 or more; returns null when invalid. */
    public static Integer integer(String s) {
        try { int i = Integer.parseInt(s.trim()); return i >= 0 ? i : null; }
        catch (Exception e) { return null; }
    }

    public static String hash(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
