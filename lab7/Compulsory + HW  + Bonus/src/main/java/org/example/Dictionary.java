package org.example;

import java.nio.file.*;
import java.util.*;

public class Dictionary {
    private final Set<String> words = new HashSet<>();
    private final PrefixTree prefixTree = new PrefixTree();

    public Dictionary() {
        try {
            List<String> wordList = Files.readAllLines(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("dictionary.txt")).toURI()));
            for (String word : wordList) {
                String trimmed = word.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    words.add(trimmed);
                    prefixTree.insert(trimmed);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isWord(String word) {
        return words.contains(word);
    }

    public List<String> getWordsWithPrefix(String prefix) {
        return prefixTree.getWordsWithPrefix(prefix);
    }
}
