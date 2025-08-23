package com.wordbrain2.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "game")
@Data
public class GameConfig {
    
    private RoomConfig room = new RoomConfig();
    private LevelConfig level = new LevelConfig();
    private GridConfig grid = new GridConfig();
    private ScoreConfig score = new ScoreConfig();
    private BoosterConfig booster = new BoosterConfig();
    private DictionaryConfig dictionary = new DictionaryConfig();
    
    @Data
    public static class RoomConfig {
        private int codeLength = 6;
        private int maxPlayers = 20;
        private long idleTimeout = 300000; // 5 minutes
    }
    
    @Data
    public static class LevelConfig {
        private int defaultDuration = 30;
        private int defaultCount = 10;
        private int transitionTime = 5;
    }
    
    @Data
    public static class GridConfig {
        private int minSize = 3;
        private int maxSize = 8;
    }
    
    @Data
    public static class ScoreConfig {
        private int basePoints = 1000;
        private double speedMultiplierMin = 0.5;
        private double speedMultiplierMax = 1.0;
        private double streakBonus = 0.1;
        private double streakMaxMultiplier = 1.5;
        private int penaltyWrong = -150;
        private int penaltyMax = 2;
    }
    
    @Data
    public static class BoosterConfig {
        private int doubleUpCooldown = 3;
        private int freezeDuration = 3000;
        private boolean freezeShieldBlocks = true;
        private int revealCost = -100;
        private int timePlusSeconds = 5;
        private int timePlusMaxUses = 2;
        private double skipHalfPointsRatio = 0.5;
    }
    
    @Data
    public static class DictionaryConfig {
        private String path = "classpath:dictionaries/";
        private String defaultLanguage = "vietnamese";
        private int cacheSize = 10000;
        private boolean preload = true;
        private int minWordLength = 3;
    }
}