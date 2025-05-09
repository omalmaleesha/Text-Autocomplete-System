package com.edu;

import java.util.*;

public class Trie {
    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

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

    public boolean search(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        TrieNode node = getNode(word.toLowerCase());
        return node != null && node.isEndOfWord();
    }

    public boolean startsWith(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return false;
        }
        return getNode(prefix.toLowerCase()) != null;
    }

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

    public List<String> getSuggestions(String prefix, int limit) {
        if (prefix == null || prefix.isEmpty()) {
            return Collections.emptyList();
        }

        prefix = prefix.toLowerCase();
        TrieNode prefixNode = getNode(prefix);
        if (prefixNode == null) {
            return Collections.emptyList();
        }

        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
        Map<String, Integer> suggestions = new HashMap<>();
        findAllWords(prefixNode, new StringBuilder(prefix), suggestions);

        for (Map.Entry<String, Integer> entry : suggestions.entrySet()) {
            pq.offer(entry);
            if (pq.size() > limit) {
                pq.poll();
            }
        }

        List<String> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            result.add(0, pq.poll().getKey());
        }
        return result;
    }

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

    public List<String> getFuzzySuggestions(String prefix, int maxDistance, int limit) {
        if (prefix == null || prefix.isEmpty()) {
            return Collections.emptyList();
        }

        prefix = prefix.toLowerCase();
        int[] vector = new int[prefix.length() + 1];
        for (int i = 0; i <= prefix.length(); i++) {
            vector[i] = i;
        }

        List<Map.Entry<String, Integer>> candidates = new ArrayList<>();
        fuzzySearch(root, new StringBuilder(), prefix, vector, maxDistance, candidates);

        candidates.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < limit && i < candidates.size(); i++) {
            suggestions.add(candidates.get(i).getKey());
        }
        return suggestions;
    }

    private void fuzzySearch(TrieNode node, StringBuilder currentPrefix, String prefix,
                             int[] vector, int maxDistance, List<Map.Entry<String, Integer>> results) {
        if (vector[prefix.length()] <= maxDistance) {
            collectWords(node, new StringBuilder(currentPrefix), results);
        }

        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            char c = entry.getKey();
            TrieNode child = entry.getValue();
            int[] newVector = new int[vector.length];
            newVector[0] = vector[0] + 1;

            for (int j = 1; j <= prefix.length(); j++) {
                int cost = (prefix.charAt(j - 1) == c) ? 0 : 1;
                newVector[j] = Math.min(Math.min(newVector[j - 1] + 1, vector[j] + 1), vector[j - 1] + cost);
            }

            currentPrefix.append(c);
            fuzzySearch(child, currentPrefix, prefix, newVector, maxDistance, results);
            currentPrefix.deleteCharAt(currentPrefix.length() - 1);
        }
    }

    private void collectWords(TrieNode node, StringBuilder prefix, List<Map.Entry<String, Integer>> results) {
        if (node.isEndOfWord()) {
            results.add(new AbstractMap.SimpleEntry<>(prefix.toString(), node.getFrequency()));
        }
        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            char c = entry.getKey();
            prefix.append(c);
            collectWords(entry.getValue(), prefix, results);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    public List<String> getPhoneticSuggestions(String prefix, int limit) {
        if (prefix == null || prefix.isEmpty()) {
            return Collections.emptyList();
        }

        prefix = prefix.toLowerCase();
        String prefixSoundex = soundex(prefix);
        List<Map.Entry<String, Integer>> candidates = new ArrayList<>();
        collectPhoneticWords(root, new StringBuilder(), prefixSoundex, candidates);

        candidates.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < limit && i < candidates.size(); i++) {
            suggestions.add(candidates.get(i).getKey());
        }
        return suggestions;
    }

    private void collectPhoneticWords(TrieNode node, StringBuilder currentWord, String targetSoundex,
                                      List<Map.Entry<String, Integer>> results) {
        if (node.isEndOfWord()) {
            String word = currentWord.toString();
            if (soundex(word).equals(targetSoundex)) {
                results.add(new AbstractMap.SimpleEntry<>(word, node.getFrequency()));
            }
        }
        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            char c = entry.getKey();
            currentWord.append(c);
            collectPhoneticWords(entry.getValue(), currentWord, targetSoundex, results);
            currentWord.deleteCharAt(currentWord.length() - 1);
        }
    }

    private String soundex(String s) {
        if (s == null || s.isEmpty()) return "";
        char[] x = s.toUpperCase().toCharArray();
        char firstLetter = x[0];
        for (int i = 0; i < x.length; i++) {
            switch (x[i]) {
                case 'B': case 'F': case 'P': case 'V': x[i] = '1'; break;
                case 'C': case 'G': case 'J': case 'K': case 'Q': case 'S': case 'X': case 'Z': x[i] = '2'; break;
                case 'D': case 'T': x[i] = '3'; break;
                case 'L': x[i] = '4'; break;
                case 'M': case 'N': x[i] = '5'; break;
                case 'R': x[i] = '6'; break;
                default: x[i] = '0'; break;
            }
        }
        StringBuilder output = new StringBuilder("" + firstLetter);
        int prevCode = x[0];
        for (int i = 1; i < x.length && output.length() < 4; i++) {
            if (x[i] != '0' && x[i] != prevCode) {
                output.append(x[i]);
            }
            prevCode = x[i];
        }
        while (output.length() < 4) {
            output.append('0');
        }
        return output.toString();
    }

    public int getFrequency(String word) {
        TrieNode node = getNode(word);
        if (node != null && node.isEndOfWord()) {
            return node.getFrequency();
        }
        return 0;
    }
}