package com.bookstore.util;

import java.util.List;

public class IdGenerator {
    /** Returns the next id such as B9 given existing ids and a prefix. */
    public static String next(String prefix, List<String> existingIds) {
        int max = 0;
        for (String id : existingIds) {
            try { max = Math.max(max, Integer.parseInt(id.substring(prefix.length()))); }
            catch (Exception ignored) { }
        }
        return prefix + (max + 1);
    }
}
