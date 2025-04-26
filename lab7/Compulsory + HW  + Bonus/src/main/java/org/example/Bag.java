package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Bag {
    private final List<Tile> tiles = new ArrayList<>();
    Random random = new Random();

    public Bag() {
        for(char c='a'; c<='z'; c++) {
            tiles.add(new Tile(c, random.nextInt(10)));
        }
    }

    public synchronized List<Tile> extractTiles(int howMany) {
        List<Tile> extracted = new ArrayList<>();
        for (int i = 0; i < howMany; i++) {
            if (tiles.isEmpty()) {
                break;
            }

            int index = random.nextInt(tiles.size());
            extracted.add(tiles.get(index));
            tiles.remove(index);
        }
        return extracted;
    }
}
