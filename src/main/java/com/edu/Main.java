package com.edu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) {
        try {
            AutocompleteSystem autocomplete = new AutocompleteSystem(5);
            System.out.println("=== Text Autocomplete System ===");
            System.out.println("Type text to get suggestions (e.g., 'hello w' for context-aware) or 'exit' to quit");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print("\nEnter text: ");
                String input = reader.readLine().trim();
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                if (input.isEmpty()) {
                    System.out.println("Please enter valid text");
                    continue;
                }

                String[] words = input.split("\\s+");
                String prefix = words[words.length - 1];
                String context = words.length > 1 ? words[words.length - 2] : null;

                System.out.println("Loading suggestions...");
                CompletableFuture<List<String>> future = autocomplete.getSuggestionsAsync(prefix, context);
                future.thenAccept(suggestions -> {
                    if (suggestions.isEmpty()) {
                        System.out.println("No suggestions found for '" + prefix + "'");
                        System.out.print("Would you like to add '" + prefix + "' to the dictionary? (y/n): ");
                        try {
                            String answer = reader.readLine().trim();
                            if (answer.equalsIgnoreCase("y")) {
                                autocomplete.addWord(prefix);
                                System.out.println("'" + prefix + "' added to dictionary");
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading input: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Suggestions for '" + prefix + "'"
                                + (context != null ? " after '" + context + "'" : "") + ":");
                        for (int i = 0; i < suggestions.size(); i++) {
                            System.out.println((i + 1) + ". " + suggestions.get(i));
                        }
                        if (suggestions.size() >= autocomplete.maxSuggestions) {
                            System.out.println("More suggestions available. Type 'more' to see additional suggestions.");
                        }
                    }
                }).exceptionally(ex -> {
                    System.err.println("Error fetching suggestions: " + ex.getMessage());
                    return null;
                });

                try {
                    future.get(); // Wait for the future to complete
                    String moreInput = reader.readLine().trim();
                    if (moreInput.equalsIgnoreCase("more")) {
                        List<String> moreSuggestions = autocomplete.getSuggestions(prefix, context);
                        System.out.println("Additional suggestions:");
                        for (int i = 0; i < moreSuggestions.size(); i++) {
                            System.out.println((i + 1) + ". " + moreSuggestions.get(i));
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