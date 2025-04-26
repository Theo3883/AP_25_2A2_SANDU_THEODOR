package org.example;

public record Tile(char letter, int points) {
    @Override
    public String toString() {
        return "Tile [letter=" + letter + ", points=" + points + "]";
    }
}
