package com.crossword.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DictionaryServiceTest {

    private DictionaryService dictionaryService;

    @BeforeEach
    void setUp() {
        dictionaryService = new DictionaryService();
        dictionaryService.loadDictionary();
    }

    @Test
    void isValidWord_WithValidWord_ShouldReturnTrue() {
        // Test with default words that should be loaded
        assertTrue(dictionaryService.isValidWord("JAVA"));
        assertTrue(dictionaryService.isValidWord("GAME"));
        assertTrue(dictionaryService.isValidWord("WEB"));
    }

    @Test
    void isValidWord_WithInvalidWord_ShouldReturnFalse() {
        assertFalse(dictionaryService.isValidWord("INVALIDWORD123"));
        assertFalse(dictionaryService.isValidWord("XYZ"));
    }

    @Test
    void isValidWord_WithNullOrEmpty_ShouldReturnFalse() {
        assertFalse(dictionaryService.isValidWord(null));
        assertFalse(dictionaryService.isValidWord(""));
        assertFalse(dictionaryService.isValidWord("   "));
    }

    @Test
    void isValidWord_WithLowercase_ShouldReturnTrue() {
        // Dictionary stores uppercase, but method should handle lowercase
        assertTrue(dictionaryService.isValidWord("java"));
        assertTrue(dictionaryService.isValidWord("game"));
    }

    @Test
    void isValidWord_WithMixedCase_ShouldReturnTrue() {
        assertTrue(dictionaryService.isValidWord("JaVa"));
        assertTrue(dictionaryService.isValidWord("GaMe"));
    }

    @Test
    void containsWord_ShouldBehaveIdenticalToIsValidWord() {
        String testWord = "JAVA";
        assertEquals(dictionaryService.isValidWord(testWord), dictionaryService.containsWord(testWord));
        
        String invalidWord = "NOTAWORD";
        assertEquals(dictionaryService.isValidWord(invalidWord), dictionaryService.containsWord(invalidWord));
    }

    @Test
    void getDictionarySize_ShouldReturnPositiveNumber() {
        int size = dictionaryService.getDictionarySize();
        assertTrue(size > 0, "Dictionary should contain at least some default words");
    }

    @Test
    void getDictionary_ShouldReturnNonEmptySet() {
        var dictionary = dictionaryService.getDictionary();
        assertNotNull(dictionary);
        assertFalse(dictionary.isEmpty());
        
        // Should contain some expected default words
        assertTrue(dictionary.contains("JAVA"));
        assertTrue(dictionary.contains("GAME"));
        assertTrue(dictionary.contains("WEB"));
    }

    @Test
    void loadDictionary_ShouldLoadDefaultWordsWhenFileNotFound() {
        // This test verifies that the service gracefully falls back to default words
        // when the dictionary file is not found
        DictionaryService newService = new DictionaryService();
        newService.loadDictionary();
        
        // Should still have default words loaded
        assertTrue(newService.isValidWord("JAVA"));
        assertTrue(newService.isValidWord("SPRING"));
        assertTrue(newService.getDictionarySize() > 0);
    }
}