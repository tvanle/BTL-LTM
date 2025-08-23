package com.wordbrain2.service.game;

import com.wordbrain2.config.GameConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DictionaryService {
    
    private final GameConfig gameConfig;
    private final Map<String, Set<String>> topicDictionaries = new HashMap<>();
    private final Set<String> generalDictionary = new HashSet<>();
    
    public DictionaryService(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }
    
    @PostConstruct
    public void initialize() {
        if (gameConfig.getDictionary().isPreload()) {
            loadDictionaries();
        }
    }
    
    private void loadDictionaries() {
        // Load general dictionary
        loadDictionary("vietnamese.txt", generalDictionary);
        
        // Load topic-specific dictionaries
        loadTopicDictionary("animals", "topics/animals.txt");
        loadTopicDictionary("technology", "topics/technology.txt");
        loadTopicDictionary("food", "topics/food.txt");
        loadTopicDictionary("science", "topics/science.txt");
        
        log.info("Loaded {} words in general dictionary", generalDictionary.size());
    }
    
    private void loadDictionary(String filename, Set<String> dictionary) {
        try {
            ClassPathResource resource = new ClassPathResource("dictionaries/" + filename);
            if (resource.exists()) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream()))) {
                    reader.lines()
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .filter(word -> word.length() >= gameConfig.getDictionary().getMinWordLength())
                        .forEach(dictionary::add);
                }
            }
        } catch (Exception e) {
            log.warn("Could not load dictionary: {}", filename);
            // Add some default words for testing
            dictionary.addAll(Arrays.asList(
                "HELLO", "WORLD", "GAME", "PLAY", "WIN", "LOSE",
                "WORD", "BRAIN", "PUZZLE", "SOLVE", "THINK", "SMART",
                "JAVA", "CODE", "WEB", "SOCKET", "SPRING", "BOOT"
            ));
        }
    }
    
    private void loadTopicDictionary(String topic, String filename) {
        Set<String> topicWords = new HashSet<>();
        loadDictionary(filename, topicWords);
        topicDictionaries.put(topic, topicWords);
    }
    
    public boolean isValidWord(String word) {
        if (word == null || word.length() < gameConfig.getDictionary().getMinWordLength()) {
            return false;
        }
        
        String upperWord = word.toUpperCase();
        
        // Check in general dictionary
        if (generalDictionary.contains(upperWord)) {
            return true;
        }
        
        // Check in all topic dictionaries
        return topicDictionaries.values().stream()
            .anyMatch(dict -> dict.contains(upperWord));
    }
    
    public List<String> getRandomWords(String topic, int count, int maxLength) {
        Set<String> dictionary = topicDictionaries.getOrDefault(topic, generalDictionary);
        
        if (dictionary.isEmpty()) {
            // Return some default words for testing
            return Arrays.asList("HELLO", "WORLD", "GAME");
        }
        
        List<String> validWords = dictionary.stream()
            .filter(word -> word.length() <= maxLength)
            .collect(Collectors.toList());
        
        Collections.shuffle(validWords);
        
        return validWords.stream()
            .limit(count)
            .collect(Collectors.toList());
    }
    
    public Set<String> getWordsByTopic(String topic) {
        return topicDictionaries.getOrDefault(topic, generalDictionary);
    }
}