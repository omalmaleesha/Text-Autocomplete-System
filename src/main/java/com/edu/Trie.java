package com.edu;

import java.util.*;

public class Trie {
    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    /**
     * Inserts a word into the trie
     * @param word The word to insert
     */
    public void insert(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }

        TrieNode current = root;
        for (char c : word.toLowerCase().toCharArray()) {
            current.getChildren().putIfAbsent(c, new TrieNode());
            current = current.getChildren().get(c);
        }
        current.setEndOfWord(true);
        current.incrementFrequency();
    }

    /**
     * Searches for a word in the trie
     * @param word The word to search for
     * @return true if the word exists, false otherwise
     */
    public boolean search(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }

        TrieNode node = getNode(word.toLowerCase());
        return node != null && node.isEndOfWord();
    }

    /**
     * Checks if there is any word that starts with the given prefix
     * @param prefix The prefix to check
     * @return true if there is a word with this prefix, false otherwise
     */
    public boolean startsWith(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return false;
        }

        return getNode(prefix.toLowerCase()) != null;
    }

    /**
     * Gets the node at the end of the prefix path
     * @param prefix The prefix to find
     * @return The TrieNode at the end of the prefix path, or null if not found
     */
    private TrieNode getNode(String prefix) {
        TrieNode current = root;
        for (char c : prefix.toCharArray()) {
            if (!current.getChildren().containsKey(c)) {
                return null;
            }
            current = current.getChildren().get(c);
        }
        return current;
    }

    /**
     * Returns a list of suggestions based on the given prefix
     * @param prefix The prefix to get suggestions for
     * @param limit The maximum number of suggestions to return
     * @return A list of suggested words
     */
    public List<String> getSuggestions(String prefix, int limit) {
        if (prefix == null || prefix.isEmpty()) {
            return Collections.emptyList();
        }

        prefix = prefix.toLowerCase();
        TrieNode prefixNode = getNode(prefix);
        if (prefixNode == null) {
            return Collections.emptyList();
        }

        // Use a priority queue to keep track of the most frequent words
        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));

        Map<String, Integer> suggestions = new HashMap<>();
        findAllWords(prefixNode, new StringBuilder(prefix), suggestions);

        // Fill the priority queue with the suggestions
        for (Map.Entry<String, Integer> entry : suggestions.entrySet()) {
            pq.offer(entry);
            if (pq.size() > limit) {
                pq.poll(); // Remove the least frequent
            }
        }

        // Extract results from the priority queue
        List<String> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            result.add(0, pq.poll().getKey()); // Insert at the beginning to reverse order
        }

        return result;
    }

    /**
     * Helper method to find all words starting from a node
     * @param node The current node
     * @param prefix The current word being built
     * @param suggestions Map to store word suggestions and their frequencies
     */
    private void findAllWords(TrieNode node, StringBuilder prefix, Map<String, Integer> suggestions) {
        if (node.isEndOfWord()) {
            suggestions.put(prefix.toString(), node.getFrequency());
        }

        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            char c = entry.getKey();
            prefix.append(c);
            findAllWords(entry.getValue(), prefix, suggestions);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }
}

