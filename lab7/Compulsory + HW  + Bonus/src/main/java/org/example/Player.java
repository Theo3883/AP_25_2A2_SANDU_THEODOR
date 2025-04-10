package org.example;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Player implements Runnable {
    private String name;
    private Game game;
    private boolean running;
    private int score;

    public Player(String name) {
        this.name = name;
        this.score = 0;
    }

    private boolean submitWord() throws InterruptedException {
        List<Tile> extracted = game.getBag().extractTiles(7);
        if (extracted.isEmpty()) {
            return false;
        }

        List<String> possibleWords = new ArrayList<>();
        generateCombinations("", extracted, possibleWords);

        for (String word : possibleWords) {
            Thread.sleep(1);
            System.out.println("Player " + name + " is trying word: " + word);
            if (game.getDictionary().isWord(word)) {
                game.getBoard().addWord(this, word);
                score += calculateWordScore(word, extracted);
                removeUsedTiles(extracted, word);
                return true; // Word successfully submitted
            }
        }

        // If no valid word is found, discard all tiles and extract new ones
        game.getBag().extractTiles(extracted.size());
        return false;
    }

    private void generateCombinations(String prefix, List<Tile> tiles, List<String> combinations) {
        if (prefix.length() >= 2) {
            combinations.add(prefix);
        }
        for (int i = 0; i < tiles.size(); i++) {
            Tile tile = tiles.get(i);
            List<Tile> remaining = new ArrayList<>(tiles);
            remaining.remove(i);
            generateCombinations(prefix + tile.getLetter(), remaining, combinations);
        }
    }

    private int calculateWordScore(String word, List<Tile> tiles) {
        int score = 0;
        for (char c : word.toCharArray()) {
            for (Tile tile : tiles) {
                if (tile.getLetter() == c) {
                    score += tile.getPoints();
                    tiles.remove(tile);
                    break;
                }
            }
        }
        return score;
    }

    private void removeUsedTiles(List<Tile> extracted, String word) {
        for (char c : word.toCharArray()) {
            extracted.removeIf(tile -> tile.getLetter() == c);
        }
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            game.waitForTurn(this);
            try {
                if (!submitWord()) {
                    if (game.getBag().extractTiles(7).isEmpty()) {
                        running = false; // Stop if no tiles are left
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            game.nextTurn();
        }
    }
}