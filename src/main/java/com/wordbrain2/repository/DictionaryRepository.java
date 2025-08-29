package com.wordbrain2.repository;

import com.wordbrain2.model.entity.Dictionary;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DictionaryRepository {
    
    private final Map<String, Dictionary> dictionaries = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> wordsByLanguage = new ConcurrentHashMap<>();
    
    public Dictionary save(Dictionary dictionary) {
        dictionaries.put(dictionary.getLanguage(), dictionary);
        wordsByLanguage.put(dictionary.getLanguage(), dictionary.getWords());
        return dictionary;
    }
    
    public Optional<Dictionary> findByLanguage(String language) {
        return Optional.ofNullable(dictionaries.get(language));
    }
    
    public boolean isValidWord(String word, String language) {
        Set<String> words = wordsByLanguage.get(language);
        return words != null && words.contains(word.toLowerCase());
    }
    
    public Set<String> getWordsByLanguage(String language) {
        return wordsByLanguage.get(language);
    }
    
    public Set<String> getAvailableLanguages() {
        return dictionaries.keySet();
    }
    
    public long getWordCount(String language) {
        Set<String> words = wordsByLanguage.get(language);
        return words != null ? words.size() : 0;
    }
    
    public void preloadDictionary(String language, Set<String> words) {
        Dictionary dictionary = new Dictionary(language, words);
        save(dictionary);
    }
    
    public void clear() {
        dictionaries.clear();
        wordsByLanguage.clear();
    }
}