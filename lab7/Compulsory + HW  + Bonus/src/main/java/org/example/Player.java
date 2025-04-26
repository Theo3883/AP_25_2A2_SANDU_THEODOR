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
        if (extracted.size() < 7) {
            return false;
        }

        List<String> possibleWords = new ArrayList<>();
        generateCombinations("", extracted, possibleWords);

        possibleWords.sort((a, b) -> Integer.compare(calculateWordScore(b, extracted), calculateWordScore(a, extracted)));

        for (String word : possibleWords) {
            if (game.getDictionary().isWord(word)) {
                game.getBoard().addWord(this, word);
                score += calculateWordScore(word, extracted);
                removeUsedTiles(extracted, word);
                System.out.println("Player " + name + " found the word: " + word);
                return true;
            }
        }

        game.getBag().extractTiles(extracted.size());
        return false;
    }


    private void generateCombinations(String prefix, List<Tile> tiles, List<String> combinations) {
        if (!game.getDictionary().getPrefixTree().isPrefix(prefix)) {
            return;
        }
        if (prefix.length() >= 2 && game.getDictionary().isWord(prefix)) {
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
        List<Tile> copy = new ArrayList<>(tiles);
        int score = 0;
        for (char c : word.toCharArray()) {
            for (Tile tile : copy) {
                if (tile.letter() == c) {
                    score += tile.points();
                    copy.remove(tile);
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