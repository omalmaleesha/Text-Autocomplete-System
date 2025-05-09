package com.edu;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutocompleteSystem {
    private final Trie trie;
    final int maxSuggestions;
    private final Map<String, Map<String, Integer>> bigrams;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AutocompleteSystem(String dictionaryPath, String corpusPath, int maxSuggestions) throws IOException {
        this.trie = new Trie();
        this.bigrams = new HashMap<>();
        this.maxSuggestions = maxSuggestions;
        DictionaryLoader.loadFromFile(trie, dictionaryPath);
        if (corpusPath != null && !corpusPath.isEmpty()) {
            DictionaryLoader.loadCorpus(trie, bigrams, corpusPath);
        } else {
            DictionaryLoader.loadDefaultCorpus(trie, bigrams);
        }
    }

    public AutocompleteSystem(int maxSuggestions) {
        this.trie = new Trie();
        this.bigrams = new HashMap<>();
        this.maxSuggestions = maxSuggestions;
        DictionaryLoader.loadDefaultDictionary(trie);
        DictionaryLoader.loadDefaultCorpus(trie, bigrams);
    }

    public void addWord(String word) {
        if (word != null && !word.isEmpty()) {
            trie.insert(word);
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
        List<String> fuzzy = trie.getFuzzySuggestions(prefix, 1, maxSuggestions - exact.size());
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
        List<String> candidates = trie.getSuggestions(prefix, Integer.MAX_VALUE);
        Map<String, Integer> following = bigrams.getOrDefault(context.toLowerCase(), Collections.emptyMap());

        List<Map.Entry<String, Integer>> withContext = new ArrayList<>();
        List<String> withoutContext = new ArrayList<>();

        for (String candidate : candidates) {
            int bigramFreq = following.getOrDefault(candidate, 0);
            if (bigramFreq > 0) {
                withContext.add(new AbstractMap.SimpleEntry<>(candidate, bigramFreq));
            } else {
                withoutContext.add(candidate);
            }
        }

        withContext.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        List<Map.Entry<String, Integer>> withoutContextScored = new ArrayList<>();
        for (String word : withoutContext) {
            int freq = trie.getFrequency(word);
            withoutContextScored.add(new AbstractMap.SimpleEntry<>(word, freq));
        }
        withoutContextScored.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        List<String> suggestions = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : withContext) {
            suggestions.add(entry.getKey());
        }
        for (Map.Entry<String, Integer> entry : withoutContextScored) {
            suggestions.add(entry.getKey());
        }
        return suggestions;
    }

    public boolean containsWord(String word) {
        return trie.search(word);
    }

    public void shutdown() {
        executor.shutdown();
    }
}