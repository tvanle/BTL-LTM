package com.wordbrain2.util;

import java.util.*;

public class WordShuffler {
    private static final Random random = new Random();
    
    public static String shuffleWord(String word) {
        List<Character> chars = new ArrayList<>();
        for (char c : word.toCharArray()) {
            chars.add(c);
        }
        Collections.shuffle(chars);
        
        StringBuilder shuffled = new StringBuilder();
        for (char c : chars) {
            shuffled.append(c);
        }
        return shuffled.toString();
    }
    
    public static List<String> shuffleWords(List<String> words) {
        List<String> shuffled = new ArrayList<>(words);
        Collections.shuffle(shuffled);
        return shuffled;
    }
    
    public static List<String> generateAnagrams(String word) {
        List<String> anagrams = new ArrayList<>();
        generateAnagramsHelper("", word, anagrams);
        return anagrams;
    }
    
    private static void generateAnagramsHelper(String prefix, String remaining, List<String> anagrams) {
        if (remaining.length() == 0) {
            anagrams.add(prefix);
        } else {
            for (int i = 0; i < remaining.length(); i++) {
                String newPrefix = prefix + remaining.charAt(i);
                String newRemaining = remaining.substring(0, i) + remaining.substring(i + 1);
                generateAnagramsHelper(newPrefix, newRemaining, anagrams);
            }
        }
    }
    
    public static String randomizeCharacters(String text, double ratio) {
        char[] chars = text.toCharArray();
        int numToRandomize = (int)(chars.length * ratio);
        
        Set<Integer> positions = new HashSet<>();
        while (positions.size() < numToRandomize) {
            positions.add(random.nextInt(chars.length));
        }
        
        for (int pos : positions) {
            chars[pos] = (char)('A' + random.nextInt(26));
        }
        
        return new String(chars);
    }
}