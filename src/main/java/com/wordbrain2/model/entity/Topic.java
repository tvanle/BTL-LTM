package com.wordbrain2.model.entity;

import lombok.Data;
import java.util.List;

@Data
public class Topic {
    
    private String id;
    private String name;
    private String description;
    private List<String> keywords;
    private String difficulty;
    private boolean isActive;
    
    public Topic() {}
    
    public Topic(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isActive = true;
    }
    
    public Topic(String id, String name, String description, List<String> keywords, String difficulty) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.keywords = keywords;
        this.difficulty = difficulty;
        this.isActive = true;
    }
}