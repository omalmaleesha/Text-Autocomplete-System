package com.edu;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    private final Map<Character, TrieNode> children;
    private boolean isEndOfWord;
    private int frequency;  // Tracks how many times this word has been seen

    public TrieNode() {
        this.children = new HashMap<>();
        this.isEndOfWord = false;
        this.frequency = 0;
    }

    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public boolean isEndOfWord() {
        return isEndOfWord;
    }

    public void setEndOfWord(boolean endOfWord) {
        isEndOfWord = endOfWord;
    }

    public int getFrequency() {
        return frequency;
    }

    public void incrementFrequency() {
        this.frequency++;
    }
}
