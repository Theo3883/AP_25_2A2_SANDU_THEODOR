package org.example;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Dictionary {

    Set<String> words = new HashSet<>();

    public Dictionary() {
        // Add a larger set of words to the dictionary
        String[] wordList = {
                "apple", "banana", "orange", "grape", "kiwi", "pear", "peach", "plum", "mango", "melon",
                "berry", "cherry", "lemon", "lime", "apricot", "fig", "date", "papaya", "guava", "coconut",
                "pineapple", "strawberry", "blueberry", "raspberry", "blackberry", "watermelon", "nectarine",
                "pomegranate", "tangerine", "cranberry", "passionfruit", "lychee", "persimmon", "dragonfruit",
                "durian", "jackfruit", "avocado", "cucumber", "tomato", "carrot", "onion", "garlic", "potato",
                "pepper", "spinach", "broccoli", "cauliflower", "lettuce", "cabbage", "zucchini", "squash",
                "pumpkin", "radish", "beet", "turnip", "celery", "parsley", "basil", "thyme", "rosemary",
                "oregano", "mint", "dill", "chive", "sage", "cilantro", "arugula", "kale", "chard", "endive"
        };

        words.addAll(Arrays.asList(wordList));
    }

    public boolean isWord(String word) {
       // return true;
        return words.contains(word);
    }
}