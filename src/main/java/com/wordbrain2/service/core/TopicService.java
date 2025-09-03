package com.wordbrain2.service.core;

import com.wordbrain2.model.entity.Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TopicService {
    
    private final Map<String, Topic> topics = new ConcurrentHashMap<>();
    private final Map<String, List<String>> topicWords = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void loadTopics() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:dictionaries/topics/*.txt");
            
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename != null && filename.endsWith(".txt")) {
                    String topicId = filename.replace(".txt", "");
                    List<String> words = loadWordsFromResource(resource);
                    
                    if (!words.isEmpty()) {
                        topicWords.put(topicId, words);
                        Topic topic = createTopicFromFile(topicId, words);
                        topics.put(topicId, topic);
                        log.info("Loaded topic '{}' with {} words", topicId, words.size());
                    }
                }
            }
            
            log.info("Successfully loaded {} topics", topics.size());
        } catch (IOException e) {
            log.error("Error loading topics from files", e);
            initializeDefaultTopics();
        }
    }
    
    private List<String> loadWordsFromResource(Resource resource) {
        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    words.add(line.toLowerCase());
                }
            }
        } catch (IOException e) {
            log.error("Error reading resource: {}", resource.getFilename(), e);
        }
        return words;
    }
    
    private Topic createTopicFromFile(String topicId, List<String> words) {
        String displayName = formatTopicName(topicId);
        String description = generateDescription(topicId, words);
        String difficulty = determineDifficulty(topicId, words);
        
        // Get first 6 words as keywords
        List<String> keywords = words.stream()
            .limit(6)
            .collect(Collectors.toList());
        
        Topic topic = new Topic(topicId, displayName, description, keywords, difficulty);
        topic.setActive(true);
        return topic;
    }
    
    private String formatTopicName(String topicId) {
        return topicId.substring(0, 1).toUpperCase() + 
               topicId.substring(1).replaceAll("_", " ");
    }
    
    private String generateDescription(String topicId, List<String> words) {
        switch (topicId) {
            case "animals":
                return "Common animals and pets";
            case "food":
                return "Food items and cooking terms";
            case "science":
                return "Scientific terms and concepts";
            case "technology":
                return "Computer and technology terms";
            default:
                return "Words related to " + formatTopicName(topicId);
        }
    }
    
    private String determineDifficulty(String topicId, List<String> words) {
        double avgLength = words.stream()
            .mapToInt(String::length)
            .average()
            .orElse(5.0);
        
        // Determine difficulty based on topic and average word length
        if (topicId.equals("animals") || topicId.equals("food") || avgLength < 5) {
            return "easy";
        } else if (topicId.equals("science") || topicId.equals("technology") || avgLength > 7) {
            return "hard";
        } else {
            return "medium";
        }
    }
    
    private void initializeDefaultTopics() {
        log.info("Initializing with default topics as fallback");
        
        // Default topics if file loading fails
        topics.put("general", new Topic("general", "General", 
            "General vocabulary words", 
            Arrays.asList("word", "game", "play", "fun", "brain", "puzzle"), 
            "easy"));
        
        topicWords.put("general", Arrays.asList(
            "word", "game", "play", "fun", "brain", "puzzle", "quiz", 
            "test", "learn", "study", "think", "solve", "answer"
        ));
    }
    
    public List<Topic> getAllTopics() {
        return new ArrayList<>(topics.values());
    }
    
    public Optional<Topic> getTopicById(String topicId) {
        return Optional.ofNullable(topics.get(topicId));
    }
    
    public List<Topic> getTopicsByDifficulty(String difficulty) {
        return topics.values().stream()
            .filter(topic -> difficulty.equals(topic.getDifficulty()))
            .filter(Topic::isActive)
            .collect(Collectors.toList());
    }
    
    public List<Topic> searchTopics(String query) {
        String lowerQuery = query.toLowerCase();
        return topics.values().stream()
            .filter(Topic::isActive)
            .filter(topic -> 
                topic.getName().toLowerCase().contains(lowerQuery) ||
                topic.getDescription().toLowerCase().contains(lowerQuery) ||
                (topic.getKeywords() != null && topic.getKeywords().stream()
                    .anyMatch(keyword -> keyword.contains(lowerQuery))))
            .collect(Collectors.toList());
    }
    
    public Topic getRandomTopic() {
        List<Topic> activeTopics = topics.values().stream()
            .filter(Topic::isActive)
            .collect(Collectors.toList());
        
        if (activeTopics.isEmpty()) {
            return null;
        }
        
        int randomIndex = new Random().nextInt(activeTopics.size());
        return activeTopics.get(randomIndex);
    }
    
    public List<String> getWordsForTopic(String topicId) {
        return topicWords.getOrDefault(topicId, Collections.emptyList());
    }
    
    public List<String> getDefaultWords() {
        // Return default words from general topic or first available topic
        if (topicWords.containsKey("general")) {
            return topicWords.get("general");
        }
        
        // Return first available topic words
        return topicWords.values().stream()
            .findFirst()
            .orElse(Arrays.asList("WORD", "GAME", "PLAY", "FUN", "BRAIN", "PUZZLE"));
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Long> difficultyCount = topics.values().stream()
            .filter(Topic::isActive)
            .collect(Collectors.groupingBy(
                Topic::getDifficulty, 
                Collectors.counting()));
        
        long totalWords = topicWords.values().stream()
            .mapToLong(List::size)
            .sum();
        
        return Map.of(
            "totalTopics", topics.values().stream().filter(Topic::isActive).count(),
            "totalWords", totalWords,
            "difficultyBreakdown", difficultyCount,
            "availableDifficulties", Arrays.asList("easy", "medium", "hard", "expert"),
            "topicsWithWords", topicWords.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().size()
                ))
        );
    }
    
    public void reloadTopics() {
        topics.clear();
        topicWords.clear();
        loadTopics();
    }
}