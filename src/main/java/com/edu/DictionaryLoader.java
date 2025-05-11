package com.edu;

import java.io.*;
import java.util.*;

public class DictionaryLoader {
    public static void loadFromFile(Trie trie, String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (!word.isEmpty()) {
                    trie.insert(word);
                }
            }
        }
    }

    public static void loadCorpus(Trie trie, Map<String, Map<String, Integer>> bigrams, String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.trim().toLowerCase().split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        trie.insert(word);
                    }
                }
                for (int i = 0; i < words.length - 1; i++) {
                    if (!words[i].isEmpty() && !words[i + 1].isEmpty()) {
                        bigrams.computeIfAbsent(words[i], k -> new HashMap<>())
                                .merge(words[i + 1], 1, Integer::sum);
                    }
                }
            }
        }
    }

    public static void loadDefaultDictionary(Trie trie) {
        String[] defaultWords = {
                "the", "be", "to", "of", "and", "a", "in", "that", "have", "I",
                "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
                "Apple", "banana", "Cat", "dog", "Elephant"
        };
        for (String word : defaultWords) {
            trie.insert(word);
        }
    }

    public static void loadDefaultCorpus(Trie trie, Map<String, Map<String, Integer>> bigrams) {
        String[] defaultSentences = {
                "the cat sat on the mat",
                "I have a dog and a cat",
                "Apple is a fruit",
                "he is not at home",
                "you do it for me"
        };
        for (String sentence : defaultSentences) {
            String[] words = sentence.toLowerCase().split("\\s+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    trie.insert(word);
                }
            }
            for (int i = 0; i < words.length - 1; i++) {
                if (!words[i].isEmpty() && !words[i + 1].isEmpty()) {
                    bigrams.computeIfAbsent(words[i], k -> new HashMap<>())
                            .merge(words[i + 1], 1, Integer::sum);
                }
            }
        }
    }
}