package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Tile {
    private final char letter;
    private final int points;

    @Override
    public String toString() {

        return "Tile [letter=" + letter + ", points=" + points + "]";
    }
}
