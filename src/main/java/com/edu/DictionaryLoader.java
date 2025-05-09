package com.edu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DictionaryLoader {
    public static void loadFromFile(Trie trie, String filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (!word.isEmpty()) {
                    trie.insert(word);
                }
            }
        }
    }

    public static void loadFromResource(Trie trie, String resourcePath) throws IOException {
        try (InputStream is = DictionaryLoader.class.getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (!word.isEmpty()) {
                    trie.insert(word);
                }
            }
        }
    }

    public static void loadDefaultDictionary(Trie trie) {
        Set<String> commonWords = new HashSet<>();
        String[] words = {
                "apple", "application", "apply", "appreciate", "approach", "appropriate",
                "banana", "band", "bank", "bar", "base", "baseball", "basic", "basis", "basket",
                "computer", "computing", "concept", "concern", "concert", "condition", "conference",
                "data", "database", "date", "daughter", "day", "deal", "death", "debate",
                "education", "effect", "effort", "eight", "either", "election", "electric",
                "function", "fund", "fundamental", "future", "gain", "galaxy", "gallery", "game",
                "hello", "help", "helpful", "her", "here", "heritage", "hero", "herself", "hide",
                "information", "infrastructure", "initial", "initially", "initiative", "injury",
                "java", "javascript", "job", "join", "joint", "joke", "journal", "journalist",
                "knowledge", "known", "label", "labor", "laboratory", "lack", "lady", "lake", "land",
                "machine", "magazine", "mail", "main", "mainly", "maintain", "maintenance",
                "network", "never", "nevertheless", "new", "newly", "news", "newspaper", "next",
                "object", "objective", "obligation", "observation", "observe", "observer", "obtain",
                "programming", "progress", "project", "projection", "promise", "promote", "prompt",
                "quality", "quarter", "quarterback", "question", "quick", "quickly", "quiet", "quietly",
                "software", "soil", "solar", "soldier", "solid", "solution", "solve", "some",
                "table", "tablet", "tackle", "tactic", "tail", "take", "tale", "talent", "talk",
                "understanding", "understood", "undertake", "uniform", "union", "unique", "unit",
                "variable", "variation", "variety", "various", "vary", "vast", "vegetable", "vehicle",
                "water", "wave", "way", "we", "weak", "wealth", "wealthy", "weapon", "wear",
                "yesterday", "yet", "yield", "you", "young", "your", "yours", "yourself", "youth"
        };
        for (String word : words) {
            trie.insert(word);
        }
    }

    public static void loadCorpus(Trie trie, Map<String, Map<String, Integer>> bigrams, String corpusPath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(corpusPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+");
                for (int i = 0; i < words.length - 1; i++) {
                    String prev = words[i];
                    String next = words[i + 1];
                    if (!prev.isEmpty() && !next.isEmpty()) {
                        bigrams.computeIfAbsent(prev, k -> new HashMap<>()).merge(next, 1, Integer::sum);
                        trie.insert(prev);
                        trie.insert(next);
                    }
                }
            }
        }
    }

    public static void loadDefaultCorpus(Trie trie, Map<String, Map<String, Integer>> bigrams) {
        String[] sentences = {
                "hello world",
                "hello there",
                "good morning",
                "good afternoon",
                "thank you",
                "please help",
                "computer science",
                "data structure",
                "java programming",
                "apple banana",
                "knowledge is power",
                "network connection",
                "software development"
        };
        for (String sentence : sentences) {
            String[] words = sentence.toLowerCase().split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                String prev = words[i];
                String next = words[i + 1];
                bigrams.computeIfAbsent(prev, k -> new HashMap<>()).merge(next, 1, Integer::sum);
                trie.insert(prev);
                trie.insert(next);
            }
        }
    }
}