package com.wordbrain2.model.entity;

import lombok.Data;
import java.util.Set;
import java.util.HashSet;

@Data
public class Dictionary {
    
    private String language;
    private Set<String> words;
    private String version;
    private long lastUpdated;
    private boolean isPreloaded;
    
    public Dictionary() {
        this.words = new HashSet<>();
        this.lastUpdated = System.currentTimeMillis();
    }
    
    public Dictionary(String language) {
        this();
        this.language = language;
    }
    
    public Dictionary(String language, Set<String> words) {
        this(language);
        this.words = new HashSet<>(words);
        this.isPreloaded = true;
    }
    
    public Dictionary(String language, Set<String> words, String version) {
        this(language, words);
        this.version = version;
    }
    
    // Word operations
    public boolean addWord(String word) {
        if (word != null && !word.trim().isEmpty()) {
            return words.add(word.toLowerCase().trim());
        }
        return false;
    }
    
    public boolean removeWord(String word) {
        if (word != null) {
            return words.remove(word.toLowerCase().trim());
        }
        return false;
    }
    
    public boolean containsWord(String word) {
        if (word != null) {
            return words.contains(word.toLowerCase().trim());
        }
        return false;
    }
    
    public void addWords(Set<String> newWords) {
        if (newWords != null) {
            newWords.stream()
                .filter(word -> word != null && !word.trim().isEmpty())
                .map(word -> word.toLowerCase().trim())
                .forEach(words::add);
        }
    }
    
    public int getWordCount() {
        return words.size();
    }
    
    public void clearWords() {
        words.clear();
    }
    
    // Override setWords to update lastUpdated
    public void setWords(Set<String> words) { 
        this.words = words != null ? new HashSet<>(words) : new HashSet<>(); 
        this.lastUpdated = System.currentTimeMillis();
    }
}