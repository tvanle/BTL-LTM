package com.wordbrain2.model.enums;

public enum CellType {
    EMPTY,      // Cell has no letter
    FILLED,     // Cell contains a letter
    BLOCKED,    // Cell is blocked/not usable
    SELECTED,   // Cell is currently selected
    REVEALED,   // Cell is revealed by hint
    CORRECT,    // Cell was part of correct word
    INCORRECT   // Cell was part of incorrect word
}