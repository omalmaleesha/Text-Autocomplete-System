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
        current.setOriginalWord(word);
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

        Map<String, Integer> suggestions = new HashMap<>();
        findAllWords(prefixNode, suggestions);

        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
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

    private void findAllWords(TrieNode node, Map<String, Integer> suggestions) {
        if (node.isEndOfWord()) {
            suggestions.put(node.getOriginalWord(), node.getFrequency());
        }
        for (TrieNode child : node.getChildren().values()) {
            findAllWords(child, suggestions);
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
        fuzzySearch(root, "", prefix, vector, maxDistance, candidates);

        candidates.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < limit && i < candidates.size(); i++) {
            suggestions.add(candidates.get(i).getKey());
        }
        return suggestions;
    }

    private void fuzzySearch(TrieNode node, String currentPrefix, String prefix,
                             int[] vector, int maxDistance, List<Map.Entry<String, Integer>> results) {
        if (vector[prefix.length()] <= maxDistance) {
            collectWords(node, results);
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

            fuzzySearch(child, currentPrefix + c, prefix, newVector, maxDistance, results);
        }
    }

    private void collectWords(TrieNode node, List<Map.Entry<String, Integer>> results) {
        if (node.isEndOfWord()) {
            results.add(new AbstractMap.SimpleEntry<>(node.getOriginalWord(), node.getFrequency()));
        }
        for (TrieNode child : node.getChildren().values()) {
            collectWords(child, results);
        }
    }

    public List<String> getPhoneticSuggestions(String prefix, int limit) {
        if (prefix == null || prefix.isEmpty()) {
            return Collections.emptyList();
        }

        prefix = prefix.toLowerCase();
        String prefixSoundex = soundex(prefix);
        List<Map.Entry<String, Integer>> candidates = new ArrayList<>();
        collectPhoneticWords(root, prefixSoundex, candidates);

        candidates.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < limit && i < candidates.size(); i++) {
            suggestions.add(candidates.get(i).getKey());
        }
        return suggestions;
    }

    private void collectPhoneticWords(TrieNode node, String targetSoundex, List<Map.Entry<String, Integer>> results) {
        if (node.isEndOfWord()) {
            String originalWord = node.getOriginalWord();
            if (soundex(originalWord).equals(targetSoundex)) {
                results.add(new AbstractMap.SimpleEntry<>(originalWord, node.getFrequency()));
            }
        }
        for (TrieNode child : node.getChildren().values()) {
            collectPhoneticWords(child, targetSoundex, results);
        }
    }

    private String soundex(String s) {
        if (s == null || s.isEmpty()) return "";
        s = s.toUpperCase();
        StringBuilder code = new StringBuilder().append(s.charAt(0));
        char prevCode = getSoundexCode(s.charAt(0));
        for (int i = 1; i < s.length() && code.length() < 4; i++) {
            char currentCode = getSoundexCode(s.charAt(i));
            if (currentCode != '0' && currentCode != prevCode) {
                code.append(currentCode);
            }
            prevCode = currentCode;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private char getSoundexCode(char c) {
        switch (Character.toUpperCase(c)) {
            case 'B': case 'F': case 'P': case 'V': return '1';
            case 'C': case 'G': case 'J': case 'K': case 'Q': case 'S': case 'X': case 'Z': return '2';
            case 'D': case 'T': return '3';
            case 'L': return '4';
            case 'M': case 'N': return '5';
            case 'R': return '6';
            default: return '0';
        }
    }

    public int getFrequency(String word) {
        TrieNode node = getNode(word.toLowerCase());
        if (node != null && node.isEndOfWord()) {
            return node.getFrequency();
        }
        return 0;
    }
}