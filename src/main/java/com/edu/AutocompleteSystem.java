package com.edu;

import java.io.IOException;
import java.util.List;

public class AutocompleteSystem {
    private final Trie trie;
    private final int maxSuggestions;

    /**
     * Creates a new AutocompleteSystem with the given dictionary file and max suggestions
     * @param dictionaryPath Path to the dictionary file
     * @param maxSuggestions Maximum number of suggestions to return
     * @throws IOException If there's an error loading the dictionary
     */
    public AutocompleteSystem(String dictionaryPath, int maxSuggestions) throws IOException {
        this.trie = new Trie();
        this.maxSuggestions = maxSuggestions;
        DictionaryLoader.loadFromFile(trie, dictionaryPath);
    }

    /**
     * Creates a new AutocompleteSystem with the default dictionary and max suggestions
     * @param maxSuggestions Maximum number of suggestions to return
     */
    public AutocompleteSystem(int maxSuggestions) {
        this.trie = new Trie();
        this.maxSuggestions = maxSuggestions;
        DictionaryLoader.loadDefaultDictionary(trie);
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
     * Gets autocomplete suggestions for a prefix
     * @param prefix The prefix to get suggestions for
     * @return A list of suggested words
     */
    public List<String> getSuggestions(String prefix) {
        return trie.getSuggestions(prefix, maxSuggestions);
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
