package org.example;

import java.util.List;

public class Timekeeper  implements  Runnable{

    private final List<Thread> playerThreads;
    private final long timeLimit;

    public Timekeeper(List<Thread> playerThreads, long timeLimit) {
        this.playerThreads = playerThreads;
        this.timeLimit = timeLimit;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeLimit;

        while (System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(1000);
             //   System.out.println("Time remaining: " + (endTime - System.currentTimeMillis()) / 1000 + " seconds");
            } catch (InterruptedException e) {
                System.out.println("Timekeeper was interrupted.");
                return;
            }
        }

        System.out.println("Time limit exceeded. Stopping all players.");
        for (Thread thread : playerThreads) {
            thread.interrupt(); //cu flaguri trebuie oprit
        }
    }
}
