package com.edu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Create an autocomplete system with default dictionary and max 5 suggestions
            AutocompleteSystem autocomplete = new AutocompleteSystem(5);

            System.out.println("=== Text Autocomplete System ===");
            System.out.println("Type a prefix to get suggestions (or 'exit' to quit)");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String input;

            while (true) {
                System.out.print("\nEnter prefix: ");
                input = reader.readLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    break;
                }

                if (input.isEmpty()) {
                    System.out.println("Please enter a valid prefix");
                    continue;
                }

                List<String> suggestions = autocomplete.getSuggestions(input);

                if (suggestions.isEmpty()) {
                    System.out.println("No suggestions found for '" + input + "'");

                    // Ask if user wants to add this word
                    System.out.print("Would you like to add this word to the dictionary? (y/n): ");
                    String answer = reader.readLine().trim();
                    if (answer.equalsIgnoreCase("y")) {
                        autocomplete.addWord(input);
                        System.out.println("'" + input + "' added to dictionary");
                    }
                } else {
                    System.out.println("Suggestions for '" + input + "':");
                    for (int i = 0; i < suggestions.size(); i++) {
                        System.out.println((i + 1) + ". " + suggestions.get(i));
                    }
                }
            }

            System.out.println("Goodbye!");

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
