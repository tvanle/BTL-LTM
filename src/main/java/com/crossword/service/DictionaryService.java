package com.crossword.service;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@Service
public class DictionaryService {
    
    private Set<String> dictionary;
    
    @PostConstruct
    public void loadDictionary() {
        dictionary = new HashSet<>();
        try {
            InputStream inputStream = getClass().getResourceAsStream("/dictionaries/words.txt");
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String word;
                while ((word = reader.readLine()) != null) {
                    dictionary.add(word.trim().toUpperCase());
                }
                reader.close();
            } else {
                // Load default words if file not found
                loadDefaultWords();
            }
        } catch (Exception e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
            loadDefaultWords();
        }
    }
    
    private void loadDefaultWords() {
        // Basic Vietnamese words for testing
        dictionary.add("ANH");
        dictionary.add("EM");
        dictionary.add("YEU");
        dictionary.add("THUONG");
        dictionary.add("GAME");
        dictionary.add("CROSSWORD");
        dictionary.add("JAVA");
        dictionary.add("SPRING");
        dictionary.add("WEB");
        dictionary.add("ONLINE");
        dictionary.add("MULTIPLAYER");
        dictionary.add("REALTIME");
    }
    
    public boolean isValidWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        return dictionary.contains(word.trim().toUpperCase());
    }
    
    public boolean containsWord(String word) {
        return isValidWord(word);
    }
    
    public int getDictionarySize() {
        return dictionary.size();
    }
    
    public Set<String> getDictionary() {
        return new HashSet<>(dictionary);
    }
}