package org.example;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrefixTree {
    private final Node root = new Node();

    private static class Node {
        private final Map<Character, Node> children = new HashMap<>();
        private boolean isWord = false;
    }

    public void insert(String word) {
        Node current = root;
        for (char c : word.toCharArray()) {
            current.children.putIfAbsent(c, new Node());
            current = current.children.get(c);
        }
        current.isWord = true;
    }

    public boolean isPrefix(String prefix) {
        Node current = root;
        for (char c : prefix.toCharArray()) {
            if (!current.children.containsKey(c)) {
                return false;
            }
            current = current.children.get(c);
        }
        return true;
    }

    public boolean isWord(String word) {
        Node current = root;
        for (char c : word.toCharArray()) {
            if (!current.children.containsKey(c)) {
                return false;
            }
            current = current.children.get(c);
        }
        return current.isWord;
    }

    public List<String> getWordsWithPrefix(String prefix) {
        List<String> results = new ArrayList<>();
        Node current = root;

        for (char c : prefix.toCharArray()) {
            if (!current.children.containsKey(c)) {
                return results; // No words with this prefix
            }
            current = current.children.get(c);
        }

        collectWords(current, new StringBuilder(prefix), results);
        return results;
    }

    private void collectWords(Node node, StringBuilder prefix, List<String> results) {
        if (node.isWord) {
            results.add(prefix.toString());
        }
        for (Map.Entry<Character, Node> entry : node.children.entrySet()) {
            prefix.append(entry.getKey());
            collectWords(entry.getValue(), prefix, results);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    public void printTree() {
        printNode(root, "");
    }

    private void printNode(Node node, String prefix) {
        if (node.isWord) {
            System.out.println("Word: " + prefix);
        }
        for (Map.Entry<Character, Node> entry : node.children.entrySet()) {
            printNode(entry.getValue(), prefix + entry.getKey());
        }
    }
}
