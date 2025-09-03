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
        // Load topic-specific dictionaries from files
        loadAllTopicDictionaries();
        
        log.info("Loaded {} topic dictionaries", topicDictionaries.size());
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
    
    
    
    public boolean isValidWord(String word) {
        if (word == null || word.length() < gameConfig.getDictionary().getMinWordLength()) {
            return false;
        }
        
        String upperWord = word.toUpperCase();
        
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
                // Cache for future use
                topicDictionaries.put(topic, dictionary);
            }
        }
        
        if (dictionary == null || dictionary.isEmpty()) {
            log.error("No words found for topic: {}", topic);
            // Get default topic words from TopicService
            return topicService.getDefaultWords();
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
            }
        }
        
        if (words == null || words.isEmpty()) {
            // Get default words from TopicService
            return topicService.getDefaultWords().stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        }
        
        return words;
    }
}