package org.example;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class Game {
    private final Bag bag = new Bag();
    private final Board board = new Board();
    private final Dictionary dictionary = new Dictionary();
    private final List<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;

    public synchronized void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        notifyAll();
    }

    public synchronized void waitForTurn(Player player) {
        while (players.get(currentPlayerIndex) != player) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.setGame(this);
    }

    public void play() {
        List<Thread> threads = new ArrayList<>();
        for (Player player : players) {
            Thread thread = new Thread(player);
            threads.add(thread);
            thread.start();
        }

        Timekeeper timekeeper = new Timekeeper(threads, 60000);
        Thread timekeeperThread = new Thread(timekeeper);
        timekeeperThread.start();

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            timekeeperThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Player winner = players.stream().max(Comparator.comparingInt(Player::getScore)).orElse(null);
        if (winner != null && winner.getScore() > 0) {
            System.out.println("The winner is " + winner.getName() + " with a score of " + winner.getScore());
        } else {
            System.out.println("No winner.");
        }
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.addPlayer(new Player("Player 1"));
        game.addPlayer(new Player("Player 2"));
        game.addPlayer(new Player("Player 3"));
        game.play();
    }
}