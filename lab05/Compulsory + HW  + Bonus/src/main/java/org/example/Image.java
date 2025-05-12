package org.example;

import java.time.LocalTime;
import java.util.ArrayList;

public record Image(String name, LocalTime date, String path, ArrayList<String> tags) {
    // Constructor overload for backward compatibility
    public Image(String name, LocalTime date, String path) {
        this(name, date, path, new ArrayList<>());
    }
}