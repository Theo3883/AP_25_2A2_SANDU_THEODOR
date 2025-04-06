package org.example;

import lombok.Getter;
import lombok.Setter;

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

    private boolean submitWord() {
        List<Tile> extracted = game.getBag().extractTiles(7);
        if (extracted.isEmpty()) {
            return false;
        }

        StringBuilder wordBuilder = new StringBuilder();
        for (Tile tile : extracted) {
            wordBuilder.append(tile.getLetter());
        }
        String word = wordBuilder.toString();

        if (game.getDictionary().isWord(word)) {
            game.getBoard().addWord(this, word);
            score += word.length(); // Assuming each letter is worth 1 point
            game.getBag().extractTiles(word.length()); // Extract new tiles
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void run() {
        running = true;
        while (running && !game.getBag().extractTiles(1).isEmpty()) {
            game.waitForTurn(this);
            if (!submitWord()) {
                game.getBag().extractTiles(7); // Discard and extract new tiles
            }
            game.nextTurn();
        }
    }
}