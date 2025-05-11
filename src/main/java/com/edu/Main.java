package com.edu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) {
        try {
            AutocompleteSystem autocomplete = new AutocompleteSystem(5);
            System.out.println("=== Text Autocomplete System ===");
            System.out.println("Type text to get suggestions (e.g., 'hello w' for context-aware)");
            System.out.println("Type 'config max N' to set max suggestions, 'config fuzzy D' to set fuzzy distance, or 'exit' to quit");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print("\nEnter text: ");
                String input = reader.readLine().trim();
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                if (input.startsWith("config ")) {
                    String[] parts = input.split("\\s+");
                    if (parts.length >= 3) {
                        String key = parts[1];
                        try {
                            int value = Integer.parseInt(parts[2]);
                            if (key.equals("max")) {
                                autocomplete.maxSuggestions = value;
                                System.out.println("Max suggestions set to " + value);
                            } else if (key.equals("fuzzy")) {
                                autocomplete.fuzzyDistance = value;
                                System.out.println("Fuzzy distance set to " + value);
                            } else {
                                System.out.println("Unknown configuration: " + key);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid value: " + parts[2]);
                        }
                    } else {
                        System.out.println("Invalid config command");
                    }
                    continue;
                }
                if (input.isEmpty()) {
                    System.out.println("Please enter valid text");
                    continue;
                }

                String[] words = input.split("\\s+");
                String prefix = words[words.length - 1];
                String context = words.length > 1 ? words[words.length - 2] : null;

                System.out.println("Loading suggestions...");
                List<String> suggestionList = new ArrayList<>();
                CompletableFuture<List<String>> future = autocomplete.getSuggestionsAsync(prefix, context);
                future.thenAccept(suggestions -> {
                    suggestionList.addAll(suggestions);
                    if (!suggestions.isEmpty()) {
                        System.out.println("Suggestions for '" + prefix + "':");
                        for (int i = 0; i < suggestions.size(); i++) {
                            System.out.println((i + 1) + ". " + suggestions.get(i));
                        }
                    }
                }).exceptionally(ex -> {
                    System.err.println("Error fetching suggestions: " + ex.getMessage());
                    return null;
                });

                try {
                    future.get();
                    if (!suggestionList.isEmpty()) {
                        System.out.println("Enter number to select, or press Enter to continue:");
                        String selection = reader.readLine().trim();
                        if (selection.matches("\\d+")) {
                            int index = Integer.parseInt(selection) - 1;
                            if (index >= 0 && index < suggestionList.size()) {
                                String selectedWord = suggestionList.get(index);
                                System.out.println("Selected: " + selectedWord);
                                autocomplete.addWord(selectedWord);
                            } else {
                                System.out.println("Invalid selection.");
                            }
                        }
                    } else {
                        List<String> corrections = autocomplete.getCorrections(prefix);
                        if (!corrections.isEmpty()) {
                            System.out.println("Did you mean:");
                            for (int i = 0; i < corrections.size(); i++) {
                                System.out.println((i + 1) + ". " + corrections.get(i));
                            }
                            System.out.println("Enter number to select, 'a' to add '" + prefix + "', or press Enter to continue:");
                            String choice = reader.readLine().trim();
                            if (choice.matches("\\d+")) {
                                int index = Integer.parseInt(choice) - 1;
                                if (index >= 0 && index < corrections.size()) {
                                    String selectedCorrection = corrections.get(index);
                                    System.out.println("Selected: " + selectedCorrection);
                                    autocomplete.addWord(selectedCorrection);
                                } else {
                                    System.out.println("Invalid selection.");
                                }
                            } else if (choice.equalsIgnoreCase("a")) {
                                autocomplete.addWord(prefix);
                                System.out.println("'" + prefix + "' added to dictionary");
                            }
                        } else {
                            System.out.println("No suggestions or corrections.");
                            System.out.print("Enter 'a' to add '" + prefix + "', or press Enter to continue: ");
                            String choice = reader.readLine().trim();
                            if (choice.equalsIgnoreCase("a")) {
                                autocomplete.addWord(prefix);
                                System.out.println("'" + prefix + "' added to dictionary");
                            }
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
            System.out.println("Goodbye!");
            autocomplete.shutdown();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}