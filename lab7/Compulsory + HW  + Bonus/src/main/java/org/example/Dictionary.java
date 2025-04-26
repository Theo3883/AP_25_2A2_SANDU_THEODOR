package org.example;

import lombok.Getter;

import java.nio.file.*;
import java.util.*;

public class Dictionary {
    @Getter
    final PrefixTree prefixTree = new PrefixTree();

    public Dictionary() {
        try {
            List<String> wordList = Files.readAllLines(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("dictionary.txt")).toURI()));
            for (String word : wordList) {
                String trimmed = word.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    prefixTree.insert(trimmed);
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
}
