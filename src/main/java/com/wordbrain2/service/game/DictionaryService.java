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
    
    public String getRandomWordByLength(String topic, int length) {
        List<String> topicWords = topicService.getWordsForTopic(topic);
        
        if (topicWords.isEmpty()) {
            log.warn("No words found for topic: {}, using all topics", topic);
            topicWords = new ArrayList<>(topicService.getAllWords());
        }
        
        // Filter by exact length
        List<String> wordsOfLength = topicWords.stream()
            .map(String::toUpperCase)
            .filter(word -> word.length() == length)
            .collect(Collectors.toList());
        
        if (wordsOfLength.isEmpty()) {
            log.warn("No words of length {} found for topic: {}", length, topic);
            // Try to find any word close to the target length
            wordsOfLength = topicWords.stream()
                .map(String::toUpperCase)
                .filter(word -> Math.abs(word.length() - length) <= 1)
                .collect(Collectors.toList());
        }
        
        if (!wordsOfLength.isEmpty()) {
            Random random = new Random();
            return wordsOfLength.get(random.nextInt(wordsOfLength.size()));
        }
        
        // Fallback: return a simple word
        return generateFallbackWord(length);
    }
    
    private String generateFallbackWord(int length) {
        String[] fallbacks = {"CAT", "DOG", "BIRD", "FISH", "TREE", "HOUSE", "WATER", "FIRE"};
        for (String word : fallbacks) {
            if (word.length() == length) {
                return word;
            }
        }
        // Generate a simple word with required length
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}