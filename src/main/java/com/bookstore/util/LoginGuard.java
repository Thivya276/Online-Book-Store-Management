package com.bookstore.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Locks a login for 10 minutes after 5 wrong passwords. Kept in memory. */
public class LoginGuard {
    private static final int MAX_FAILS = 5;
    private static final long LOCK_MS = 10 * 60 * 1000L;
    private static final Map<String, Integer> fails = new ConcurrentHashMap<>();
    private static final Map<String, Long> lockedUntil = new ConcurrentHashMap<>();

    public static boolean isLocked(String key) {
        Long t = lockedUntil.get(key);
        if (t == null) return false;
        if (System.currentTimeMillis() >= t) { lockedUntil.remove(key); fails.remove(key); return false; }
        return true;
    }

    public static long minutesLeft(String key) {
        Long t = lockedUntil.get(key);
        return t == null ? 0 : Math.max(1, (t - System.currentTimeMillis() + 59_999) / 60_000);
    }

    public static void fail(String key) {
        int n = fails.merge(key, 1, Integer::sum);
        if (n >= MAX_FAILS) lockedUntil.put(key, System.currentTimeMillis() + LOCK_MS);
    }

    public static void success(String key) { fails.remove(key); lockedUntil.remove(key); }
}
