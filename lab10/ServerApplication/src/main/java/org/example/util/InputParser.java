package org.example.util;

public class InputParser {
    private InputParser() {}

    public static int parseRowOrColumn(String input) {
        input = input.trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            if (input.length() == 1) {
                char c = Character.toUpperCase(input.charAt(0));
                if (c >= 'A' && c <= 'Z') {
                    return c - 'A';
                }
            }
            throw new IllegalArgumentException("Invalid row/column format. Use numbers or letters A-Z.");
        }
    }
}
