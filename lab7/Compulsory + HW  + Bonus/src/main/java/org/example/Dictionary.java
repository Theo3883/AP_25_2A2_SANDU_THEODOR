package org.example;

import lombok.Getter;

import java.nio.file.*;
import java.util.*;

public class Dictionary {
    @Getter
    private final PrefixTree prefixTree = new PrefixTree();
    private final List<String> words = new ArrayList<>();

    public Dictionary() {
        try {
            List<String> wordList = Files.readAllLines(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("dictionary.txt")).toURI()));
            for (String word : wordList) {
                String trimmed = word.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    prefixTree.insert(trimmed);
                    words.add(trimmed);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isWord(String word) {
        return prefixTree.isWord(word);
    }

    public List<String> getWordsWithPrefix(String prefix) {
        return prefixTree.getWordsWithPrefix(prefix);
    }

    public List<String> lookupParallel(String prefix) {
        return words.parallelStream()
                .filter(word -> word.startsWith(prefix))
                .toList();
    }
}
