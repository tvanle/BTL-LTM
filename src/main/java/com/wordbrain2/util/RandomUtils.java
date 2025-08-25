package com.wordbrain2.util;

import java.util.*;
import java.security.SecureRandom;

public class RandomUtils {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Random random = new Random();
    private static final String ROOM_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    
    public static String generateRoomCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(ROOM_CODE_CHARS.charAt(secureRandom.nextInt(ROOM_CODE_CHARS.length())));
        }
        return code.toString();
    }
    
    public static String generatePlayerId() {
        return "player_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    public static String generateSessionId() {
        return "session_" + UUID.randomUUID().toString();
    }
    
    public static <T> T randomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }
    
    public static <T> List<T> randomSample(List<T> list, int sampleSize) {
        if (list == null || list.size() <= sampleSize) {
            return new ArrayList<>(list);
        }
        
        List<T> shuffled = new ArrayList<>(list);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, sampleSize);
    }
    
    public static int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
    
    public static boolean randomBoolean(double probability) {
        return random.nextDouble() < probability;
    }
    
    public static <T> void shuffle(List<T> list) {
        Collections.shuffle(list, random);
    }
}