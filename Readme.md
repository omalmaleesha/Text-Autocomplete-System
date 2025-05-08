# Text Autocomplete System

A Java implementation of a text autocomplete system using a Trie data structure. This project demonstrates efficient word suggestion based on user input prefixes.

## Table of Contents
- [Features](#features)
- [Project Structure](#project-structure)
- [How It Works](#how-it-works)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Customization](#customization)
- [Performance](#performance)
- [Future Enhancements](#future-enhancements)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Efficient Prefix Matching**: Uses a Trie data structure to provide O(m) time complexity for lookups, where m is the length of the prefix.
- **Frequency-Based Suggestions**: Displays suggestions ranked by usage frequency, prioritizing commonly used words.
- **Dynamic Dictionary**: Allows users to add new words to the dictionary when no suggestions are found.
- **Flexible Dictionary Loading**: Supports loading dictionaries from files, resources, or a built-in default word set.
- **Customizable Suggestions**: Configurable maximum number of suggestions to display.

## Project Structure

```
src/
├── com/
│   └── autocomplete/
│       ├── Trie.java            - Core trie data structure implementation
│       ├── TrieNode.java        - Node structure for the trie
│       ├── AutocompleteSystem.java - Main autocomplete functionality 
│       ├── DictionaryLoader.java - Utility to load words into the system
│       └── Main.java            - Demo application with console interface
└── resources/
    └── dictionary.txt           - Sample dictionary file
```

## How It Works

1. The system constructs a Trie data structure where:
    - Each node represents a character
    - Paths from root to leaf form complete words
    - Each node has a flag indicating if it's the end of a word
    - Word frequency is tracked for ranking suggestions

2. When a user enters a prefix:
    - The system traverses the Trie to find the node representing the end of the prefix
    - From that node, it performs a depth-first search to collect all complete words
    - Results are sorted by frequency and limited to the configured maximum suggestions

3. The system allows adding new words, which updates the Trie and increments frequency counters for existing words.

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- Basic understanding of Java and data structures

## Installation

1. Clone this repository:
   ```
   git clone https://github.com/yourusername/text-autocomplete-system.git
   ```

2. Navigate to the project directory:
   ```
   cd text-autocomplete-system
   ```

3. Compile the code:
   ```
   javac -d bin src/com/autocomplete/*.java
   ```

## Usage

### Running the Console Application

1. Run the Main class:
   ```
   java -cp bin com.autocomplete.Main
   ```

2. Follow the on-screen instructions:
    - Type a prefix to get word suggestions
    - Enter 'exit' to quit the application
    - When no suggestions are found, you can choose to add the word to the dictionary

### Integrating in Your Own Project

```java
// Create an autocomplete system with default dictionary and max 5 suggestions
AutocompleteSystem autocomplete = new AutocompleteSystem(5);

// Or create with a custom dictionary file
// AutocompleteSystem autocomplete = new AutocompleteSystem("path/to/dictionary.txt", 5);

// Get suggestions for a prefix
List<String> suggestions = autocomplete.getSuggestions("pro");

// Add a new word
autocomplete.addWord("newword");

// Check if a word exists
boolean exists = autocomplete.containsWord("example");
```

## Customization

### Custom Dictionary

Create a text file with one word per line and load it:

```java
AutocompleteSystem autocomplete = new AutocompleteSystem("path/to/your/dictionary.txt", 5);
```

### Adjusting Maximum Suggestions

Modify the second parameter to change the maximum number of suggestions:

```java
AutocompleteSystem autocomplete = new AutocompleteSystem(10); // Show up to 10 suggestions
```

## Performance

- **Time Complexity**:
    - Word insertion: O(m) where m is the word length
    - Prefix search: O(m) where m is the prefix length
    - Collecting suggestions: O(n) where n is the number of nodes in the subtree

- **Space Complexity**:
    - O(ALPHABET_SIZE × m × n) where m is the average word length and n is the number of words

## Future Enhancements

- Implement spell checking and correction
- Add support for multi-word phrases
- Implement context-aware suggestions
- Add GUI interface
- Support for different languages and character sets
- Implement serialization to save and load the learned dictionary

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Commit your changes: `git commit -m 'Add some feature'`
4. Push to the branch: `git push origin feature-name`
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.