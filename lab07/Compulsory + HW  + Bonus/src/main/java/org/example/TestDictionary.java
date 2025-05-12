package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TestDictionary {
    public static void testSpeed(Dictionary dictionary) {
        List<String> randomPrefixes = generateRandomPrefixes(20);
        randomPrefixes.addAll(List.of("a", "e", "z", "th", "pre", "inter", "sub", "over", "under", "trans", "re", "un", "non"));

        Collections.shuffle(randomPrefixes);

        for (String prefix : randomPrefixes) {
            System.out.println("\nTesting prefix: '" + prefix + "'");

            long start1 = System.nanoTime();
            List<String> parallelResult = dictionary.lookupParallel(prefix);
            long end1 = System.nanoTime();
            long parallelTime = end1 - start1;

            long start2 = System.nanoTime();
            List<String> prefixTreeResult = dictionary.getWordsWithPrefix(prefix);
            long end2 = System.nanoTime();
            long prefixTreeTime = end2 - start2;

            System.out.println("Parallel search: " + parallelTime + " ns, found " + parallelResult.size() + " words");
            System.out.println("PrefixTree search: " + prefixTreeTime + " ns, found " + prefixTreeResult.size() + " words");

            if (!listsEqual(parallelResult, prefixTreeResult)) {
                System.out.println("Mismatch detected between search results!");
            }
        }
    }

    private static boolean listsEqual(List<String> a, List<String> b) {
        if (a.size() != b.size()) return false;
        List<String> sortedA = new ArrayList<>(a);
        List<String> sortedB = new ArrayList<>(b);
        Collections.sort(sortedA);
        Collections.sort(sortedB);
        return sortedA.equals(sortedB);
    }

    private static List<String> generateRandomPrefixes(int count) {
        Random random = new Random();
        List<String> prefixes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int length = 1 + random.nextInt(4);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < length; j++) {
                char c = (char) ('a' + random.nextInt(26));
                sb.append(c);
            }
            prefixes.add(sb.toString());
        }
        return prefixes;
    }
}
