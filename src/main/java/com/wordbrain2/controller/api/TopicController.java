package com.wordbrain2.controller.api;

import com.wordbrain2.model.entity.Topic;
import com.wordbrain2.service.core.TopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/topics")
@CrossOrigin(origins = "*")
public class TopicController {
    
    private final TopicService topicService;
    
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }
    
    @GetMapping
    public ResponseEntity<List<Topic>> getAllTopics() {
        List<Topic> topics = topicService.getAllTopics();
        return ResponseEntity.ok(topics);
    }
    
    @GetMapping("/{topicId}")
    public ResponseEntity<Topic> getTopicById(@PathVariable String topicId) {
        return topicService.getTopicById(topicId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<Topic>> getTopicsByDifficulty(@PathVariable String difficulty) {
        List<Topic> topics = topicService.getTopicsByDifficulty(difficulty);
        return ResponseEntity.ok(topics);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Topic>> searchTopics(@RequestParam String query) {
        List<Topic> searchResults = topicService.searchTopics(query);
        return ResponseEntity.ok(searchResults);
    }
    
    @GetMapping("/random")
    public ResponseEntity<Topic> getRandomTopic() {
        Topic randomTopic = topicService.getRandomTopic();
        if (randomTopic == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(randomTopic);
    }
    
    @GetMapping("/{topicId}/words")
    public ResponseEntity<Map<String, Object>> getTopicWords(@PathVariable String topicId) {
        List<String> words = topicService.getWordsForTopic(topicId);
        if (words.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(Map.of(
            "topicId", topicId,
            "words", words,
            "count", words.size()
        ));
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTopicStatistics() {
        Map<String, Object> statistics = topicService.getStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @PostMapping("/reload")
    public ResponseEntity<Map<String, String>> reloadTopics() {
        topicService.reloadTopics();
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Topics reloaded successfully"
        ));
    }
}