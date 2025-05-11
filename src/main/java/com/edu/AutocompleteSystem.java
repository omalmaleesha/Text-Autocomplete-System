package com.edu;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;

public class AutocompleteSystem {
    private final Trie trie;
    int maxSuggestions;
    private final Map<String, Map<String, Integer>> bigrams;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    int fuzzyDistance = 1;

    public AutocompleteSystem(String dictionaryPath, String corpusPath, int maxSuggestions) throws IOException {
        this.trie = new Trie();
        this.maxSuggestions = maxSuggestions;
        this.bigrams = new HashMap<>();
        DictionaryLoader.loadFromFile(trie, dictionaryPath);
        try {
            DictionaryLoader.loadFromFile(trie, "user_dictionary.txt");
        } catch (IOException e) {
            // Ignore if user dictionary doesn't exist
        }
        if (corpusPath != null && !corpusPath.isEmpty()) {
            DictionaryLoader.loadCorpus(trie, bigrams, corpusPath);
        } else {
            DictionaryLoader.loadDefaultCorpus(trie, bigrams);
        }
    }

    public AutocompleteSystem(int maxSuggestions) {
        this.trie = new Trie();
        this.maxSuggestions = maxSuggestions;
        this.bigrams = new HashMap<>();
        DictionaryLoader.loadDefaultDictionary(trie);
        try {
            DictionaryLoader.loadFromFile(trie, "user_dictionary.txt");
        } catch (IOException e) {
            // Ignore
        }
        DictionaryLoader.loadDefaultCorpus(trie, bigrams);
    }

    public void addWord(String word) {
        if (word != null && !word.isEmpty()) {
            trie.insert(word);
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("user_dictionary.txt"), StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
                writer.write(word + "\n");
            } catch (IOException e) {
                System.err.println("Error writing to user dictionary: " + e.getMessage());
            }
        }
    }

    public CompletableFuture<List<String>> getSuggestionsAsync(String prefix, String context) {
        return CompletableFuture.supplyAsync(() -> getSuggestions(prefix, context), executor);
    }

    public List<String> getSuggestions(String prefix, String context) {
        if (prefix == null || prefix.isEmpty()) {
            return Collections.emptyList();
        }

        if (context != null && !context.isEmpty()) {
            List<String> contextSuggestions = getContextSuggestions(prefix, context);
            if (!contextSuggestions.isEmpty()) {
                return contextSuggestions.subList(0, Math.min(maxSuggestions, contextSuggestions.size()));
            }
        }
        return getRegularSuggestions(prefix);
    }

    private List<String> getRegularSuggestions(String prefix) {
        List<String> exact = trie.getSuggestions(prefix, maxSuggestions);
        if (exact.size() >= maxSuggestions) {
            return exact;
        }

        Set<String> allSuggestions = new LinkedHashSet<>(exact);
        List<String> fuzzy = trie.getFuzzySuggestions(prefix, fuzzyDistance, maxSuggestions - exact.size());
        for (String f : fuzzy) {
            if (allSuggestions.size() >= maxSuggestions) break;
            allSuggestions.add(f);
        }

        if (allSuggestions.size() < maxSuggestions) {
            List<String> phonetic = trie.getPhoneticSuggestions(prefix, maxSuggestions - allSuggestions.size());
            for (String p : phonetic) {
                if (allSuggestions.size() >= maxSuggestions) break;
                allSuggestions.add(p);
            }
        }

        return new ArrayList<>(allSuggestions);
    }

    private List<String> getContextSuggestions(String prefix, String context) {
        Map<String, Integer> nextWords = bigrams.getOrDefault(context.toLowerCase(), Collections.emptyMap());
        if (nextWords.isEmpty()) {
            return getRegularSuggestions(prefix);
        }

        List<String> exactMatches = trie.getSuggestions(prefix, Integer.MAX_VALUE);
        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        for (String word : exactMatches) {
            int freq = nextWords.getOrDefault(word.toLowerCase(), 0);
            if (freq > 0) {
                pq.offer(new AbstractMap.SimpleEntry<>(word, freq));
            }
        }

        List<String> suggestions = new ArrayList<>();
        while (!pq.isEmpty() && suggestions.size() < maxSuggestions) {
            suggestions.add(pq.poll().getKey());
        }
        return suggestions;
    }

    public boolean containsWord(String word) {
        return trie.search(word);
    }

    public void shutdown() {
        executor.shutdown();
    }

    public List<String> getCorrections(String prefix) {
        List<String> fuzzy = trie.getFuzzySuggestions(prefix, fuzzyDistance, 5);
        List<String> phonetic = trie.getPhoneticSuggestions(prefix, 5);
        Set<String> corrections = new LinkedHashSet<>(fuzzy);
        corrections.addAll(phonetic);
        return new ArrayList<>(corrections).subList(0, Math.min(5, corrections.size()));
    }
}