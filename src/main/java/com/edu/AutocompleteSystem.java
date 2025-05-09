package com.edu;

import java.io.IOException;
import java.util.*;

public class AutocompleteSystem {
    private final Trie trie;
    private final int maxSuggestions;
    private final Map<String, Map<String, Integer>> bigrams; // For context-aware suggestions

    /**
     * Creates a new AutocompleteSystem with the given dictionary and corpus files
     * @param dictionaryPath Path to the dictionary file
     * @param corpusPath Path to the corpus file for bigrams (optional)
     * @param maxSuggestions Maximum number of suggestions to return
     * @throws IOException If there's an error loading files
     */
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

    /**
     * Creates a new AutocompleteSystem with default dictionary and max suggestions
     * @param maxSuggestions Maximum number of suggestions to return
     */
    public AutocompleteSystem(int maxSuggestions) {
        this.trie = new Trie();
        this.bigrams = new HashMap<>();
        this.maxSuggestions = maxSuggestions;
        DictionaryLoader.loadDefaultDictionary(trie);
        DictionaryLoader.loadDefaultCorpus(trie, bigrams);
    }

    /**
     * Adds a new word to the autocomplete system
     * @param word The word to add
     */
    public void addWord(String word) {
        if (word != null && !word.isEmpty()) {
            trie.insert(word);
        }
    }

    /**
     * Gets autocomplete suggestions for a prefix with optional context
     * @param prefix The prefix to get suggestions for
     * @param context The previous word for context-aware suggestions (optional)
     * @return A list of suggested words
     */
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

    /**
     * Gets regular suggestions combining exact, fuzzy, and phonetic matches
     * @param prefix The prefix to get suggestions for
     * @return A list of suggested words
     */
    private List<String> getRegularSuggestions(String prefix) {
        // Get exact matches first
        List<String> exact = trie.getSuggestions(prefix, maxSuggestions);
        if (exact.size() >= maxSuggestions) {
            return exact;
        }

        // Supplement with fuzzy matches (Levenshtein distance)
        Set<String> allSuggestions = new LinkedHashSet<>(exact);
        List<String> fuzzy = trie.getFuzzySuggestions(prefix, 1, maxSuggestions - exact.size());
        for (String f : fuzzy) {
            if (allSuggestions.size() >= maxSuggestions) break;
            allSuggestions.add(f);
        }

        // If still fewer, add phonetic matches (Soundex)
        if (allSuggestions.size() < maxSuggestions) {
            List<String> phonetic = trie.getPhoneticSuggestions(prefix, maxSuggestions - allSuggestions.size());
            for (String p : phonetic) {
                if (allSuggestions.size() >= maxSuggestions) break;
                allSuggestions.add(p);
            }
        }

        return new ArrayList<>(allSuggestions);
    }

    /**
     * Gets context-aware suggestions using bigrams
     * @param prefix The prefix to get suggestions for
     * @param context The previous word
     * @return A list of context-aware suggested words
     */
    private List<String> getContextSuggestions(String prefix, String context) {
        List<String> candidates = trie.getSuggestions(prefix, Integer.MAX_VALUE); // All exact matches
        Map<String, Integer> following = bigrams.getOrDefault(context.toLowerCase(), Collections.emptyMap());

        // Separate candidates into context-relevant and others
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

        // Sort context-relevant by bigram frequency
        withContext.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        // Sort others by word frequency
        List<Map.Entry<String, Integer>> withoutContextScored = new ArrayList<>();
        for (String word : withoutContext) {
            int freq = trie.getFrequency(word);
            withoutContextScored.add(new AbstractMap.SimpleEntry<>(word, freq));
        }
        withoutContextScored.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        // Combine results
        List<String> suggestions = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : withContext) {
            suggestions.add(entry.getKey());
        }
        for (Map.Entry<String, Integer> entry : withoutContextScored) {
            suggestions.add(entry.getKey());
        }
        return suggestions;
    }

    /**
     * Checks if a word exists in the system
     * @param word The word to check
     * @return true if the word exists, false otherwise
     */
    public boolean containsWord(String word) {
        return trie.search(word);
    }
}