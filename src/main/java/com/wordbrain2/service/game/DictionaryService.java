package com.wordbrain2.service.game;

import com.wordbrain2.config.GameConfig;
import com.wordbrain2.service.core.TopicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DictionaryService {
    
    private final GameConfig gameConfig;
    private final TopicService topicService;
    
    public DictionaryService(GameConfig gameConfig, TopicService topicService) {
        this.gameConfig = gameConfig;
        this.topicService = topicService;
    }
    
    public boolean isValidWord(String word, String topic) {
        if (word == null || word.length() < gameConfig.getDictionary().getMinWordLength()) {
            return false;
        }
        
        String upperWord = word.toUpperCase();
        
        // Check only in the specific topic words
        Set<String> topicWords = getWordsByTopic(topic);
        return topicWords.contains(upperWord);
    }
    
    public List<String> getRandomWords(String topic, int count, int maxLength) {
        // Get words from TopicService
        List<String> topicWords = topicService.getWordsForTopic(topic);
        
        if (topicWords.isEmpty()) {
            log.warn("No words found for topic: {}, using random topic", topic);
            var randomTopic = topicService.getRandomTopic();
            if (randomTopic != null) {
                topicWords = topicService.getWordsForTopic(randomTopic.getId());
            }
        }
        
        // Filter by max length and convert to uppercase
        List<String> validWords = topicWords.stream()
            .map(String::toUpperCase)
            .filter(word -> word.length() <= maxLength)
            .collect(Collectors.toList());
        
        Collections.shuffle(validWords);
        
        return validWords.stream()
            .limit(count)
            .collect(Collectors.toList());
    }
    
    public Set<String> getWordsByTopic(String topic) {
        List<String> words = topicService.getWordsForTopic(topic);
        
        if (words.isEmpty()) {
            log.warn("No words found for topic: {}", topic);
            return Collections.emptySet();
        }
        
        return words.stream()
            .map(String::toUpperCase)
            .collect(Collectors.toSet());
    }
}