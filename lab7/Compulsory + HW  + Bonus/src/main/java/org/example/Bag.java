package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Bag {
    private final List<Tile> tiles = new ArrayList<>();
    Random random = new Random();

    public Bag() {
        for(char c='a'; c<='z'; c++) {
            int points = random.nextInt(10);
            addTiles(new Tile(c, points), 10);
        }
    }

    private void addTiles(Tile tile, int amount) {
        for(int i = 0; i < amount; i++) {
            tiles.add(tile);
        }
    }

    public synchronized List<Tile> extractTiles(int howMany) {
        List<Tile> extractedTile = new ArrayList<>();
        for (int i = 0; i < howMany; i++) {
            if (tiles.isEmpty()) {
                break;
            }
            int index = random.nextInt(tiles.size());
            extractedTile.add(tiles.get(index));
            tiles.remove(index);
        }
        return extractedTile;
    }
}
