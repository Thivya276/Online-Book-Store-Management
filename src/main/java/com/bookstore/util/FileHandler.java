package com.bookstore.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Reads and writes the pipe-delimited text files in the data/ folder. */
public class FileHandler {
    private static final Path DIR = Paths.get("data");

    public static synchronized List<String> read(String file) {
        try {
            Files.createDirectories(DIR);
            Path p = DIR.resolve(file);
            if (!Files.exists(p)) { Files.createFile(p); return new ArrayList<>(); }
            List<String> out = new ArrayList<>();
            for (String line : Files.readAllLines(p, StandardCharsets.UTF_8)) if (!line.isBlank()) out.add(line);
            return out;
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    public static synchronized void write(String file, List<String> lines) {
        try {
            Files.createDirectories(DIR);
            Files.write(DIR.resolve(file), lines, StandardCharsets.UTF_8);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    /** Removes characters that would break the file format. */
    public static String clean(String s) {
        return s == null ? "" : s.replace("|", " ").replace("\r", " ").replace("\n", " ").trim();
    }
}
