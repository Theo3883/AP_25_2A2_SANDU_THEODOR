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
        //List<Tile> extracted = game.getBag().extractTiles(7);
        List<Tile> extracted = new ArrayList<>(List.of(
                new Tile('a', 3), new Tile('a', 1), new Tile('p', 3),
                new Tile('p', 3), new Tile('l', 1), new Tile('e', 1), new Tile('c', 3)
        ));
        if (extracted.size() < 7) {
            return false;
        }

        List<String> possibleWords = new ArrayList<>();
        generateCombinations("", extracted, possibleWords);

        for (String word : possibleWords) {
            if (game.getDictionary().isWord(word)) {
                game.getBoard().addWord(this, word);
                score += calculateWordScore(word, extracted);
                removeUsedTiles(extracted, word);
                System.out.println("Player " + name + " found the word: " + word);
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
            generateCombinations(prefix + tile.letter(), remaining, combinations);
        }
    }

    private int calculateWordScore(String word, List<Tile> tiles) {
        int score = 0;
        for (char c : word.toCharArray()) {
            for (Tile tile : tiles) {
                if (tile.letter() == c) {
                    score += tile.points();
                    tiles.remove(tile);
                    break;
                }
            }
        }
        return score;
    }

    private void removeUsedTiles(List<Tile> extracted, String word) {
        for (char c : word.toCharArray()) {
            extracted.removeIf(tile -> tile.letter() == c);
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