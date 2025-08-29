package com.wordbrain2.service.game;

import com.wordbrain2.config.GameConfig;
import com.wordbrain2.service.core.TopicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DictionaryService {
    
    private final GameConfig gameConfig;
    private final TopicService topicService;
    private final Map<String, Set<String>> topicDictionaries = new HashMap<>();
    private final Set<String> generalDictionary = new HashSet<>();
    
    public DictionaryService(GameConfig gameConfig, TopicService topicService) {
        this.gameConfig = gameConfig;
        this.topicService = topicService;
    }
    
    @PostConstruct
    public void initialize() {
        if (gameConfig.getDictionary().isPreload()) {
            loadDictionaries();
        }
    }
    
    private void loadDictionaries() {
        // Load general dictionary
        loadGeneralDictionary();
        
        // Load topic-specific dictionaries from files
        loadAllTopicDictionaries();
        
        log.info("Loaded {} words in general dictionary", generalDictionary.size());
        log.info("Loaded {} topic dictionaries", topicDictionaries.size());
    }
    
    private void loadGeneralDictionary() {
        try {
            ClassPathResource resource = new ClassPathResource("dictionaries/vietnamese.txt");
            if (resource.exists()) {
                loadFromResource(resource, generalDictionary);
            } else {
                // Try english.txt as fallback
                resource = new ClassPathResource("dictionaries/english.txt");
                if (resource.exists()) {
                    loadFromResource(resource, generalDictionary);
                }
            }
        } catch (Exception e) {
            log.warn("Could not load general dictionary, using defaults", e);
            loadDefaultWords(generalDictionary);
        }
    }
    
    private void loadAllTopicDictionaries() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:dictionaries/topics/*.txt");
            
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename != null && filename.endsWith(".txt")) {
                    String topicId = filename.replace(".txt", "");
                    Set<String> topicWords = new HashSet<>();
                    loadFromResource(resource, topicWords);
                    
                    if (!topicWords.isEmpty()) {
                        topicDictionaries.put(topicId, topicWords);
                        log.debug("Loaded {} words for topic: {}", topicWords.size(), topicId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error loading topic dictionaries", e);
        }
    }
    
    private void loadFromResource(Resource resource, Set<String> dictionary) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .map(String::toUpperCase)
                .filter(word -> word.length() >= gameConfig.getDictionary().getMinWordLength())
                .forEach(dictionary::add);
        } catch (Exception e) {
            log.warn("Could not load dictionary from resource: {}", resource.getFilename(), e);
        }
    }
    
    private void loadDefaultWords(Set<String> dictionary) {
        dictionary.addAll(Arrays.asList(
            "HELLO", "WORLD", "GAME", "PLAY", "WIN", "LOSE",
            "WORD", "BRAIN", "PUZZLE", "SOLVE", "THINK", "SMART",
            "JAVA", "CODE", "WEB", "SOCKET", "SPRING", "BOOT"
        ));
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
        // First try to get words from topic dictionaries
        Set<String> dictionary = topicDictionaries.get(topic);
        
        // If not found in dictionaries, try to get from TopicService
        if (dictionary == null || dictionary.isEmpty()) {
            List<String> topicWords = topicService.getWordsForTopic(topic);
            if (!topicWords.isEmpty()) {
                dictionary = topicWords.stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());
            } else {
                // Fall back to general dictionary
                dictionary = generalDictionary;
            }
        }
        
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
        Set<String> words = topicDictionaries.get(topic);
        
        // If not in cached dictionaries, get from TopicService
        if (words == null || words.isEmpty()) {
            List<String> topicWords = topicService.getWordsForTopic(topic);
            if (!topicWords.isEmpty()) {
                words = topicWords.stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());
                // Cache for future use
                topicDictionaries.put(topic, words);
            } else {
                words = generalDictionary;
            }
        }
        
        return words;
    }
}