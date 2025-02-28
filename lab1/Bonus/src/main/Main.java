package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        int n = 10;
        int k = 5;
        boolean[][] graph = generateRandomGraph(n, 0.5);

        if (hasClique(graph, n, k)) {
            System.out.println("The graph has a clique of size " + k + " or larger.");
        } else {
            System.out.println("The graph does not have a clique of size " + k + ".");
        }
    }

    // Generate a random graph with n vertices and edge probability p
    public static boolean[][] generateRandomGraph(int n, double p) {
        Random random = new Random();
        boolean[][] graph = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                graph[i][j] = graph[j][i] = random.nextDouble() < p;
            }
        }
        return graph;
    }


    public static boolean hasClique(boolean[][] graph, int n, int k) {
        List<Integer> currentClique = new ArrayList<>();
        return findClique(graph, n, k, 0, currentClique);
    }

    // Backtracking function to find clique
    public static boolean findClique(boolean[][] graph, int n, int k, int start, List<Integer> currentClique) {
        if (currentClique.size() == k) {
            return true;
        }
        for (int i = start; i < n; i++) {
            if (isSafe(graph, currentClique, i)) {
                currentClique.add(i);
                if (findClique(graph, n, k, i + 1, currentClique)) {
                    return true;
                }
                currentClique.removeLast();
            }
        }
        return false;
    }

    // Check if vertex v can be added to the current clique
    public static boolean isSafe(boolean[][] graph, List<Integer> currentClique, int v) {
        for (int u : currentClique) {
            if (!graph[u][v]) {
                return false;
            }
        }
        return true;
    }


    // Check if the graph has a stable set of size at least k
    public static boolean hasStableSet(boolean[][] graph, int n, int k) {
        List<Integer> currentStableSet = new ArrayList<>();
        return findStableSet(graph, n, k, 0, currentStableSet);
    }

    // Backtracking function to find stable sets
    public static boolean findStableSet(boolean[][] graph, int n, int k, int start, List<Integer> currentStableSet) {
        if (currentStableSet.size() == k) {
            return true;
        }
        for (int i = start; i < n; i++) {
            if (isSafeForStableSet(graph, currentStableSet, i)) {
                currentStableSet.add(i);
                if (findStableSet(graph, n, k, i + 1, currentStableSet)) {
                    return true;
                }
                currentStableSet.removeLast();
            }
        }
        return false;
    }

    // Check if vertex v can be added to the current stable set
    public static boolean isSafeForStableSet(boolean[][] graph, List<Integer> currentStableSet, int v) {
        for (int u : currentStableSet) {
            if (graph[u][v]) {
                return false;
            }
        }
        return true;
    }
}