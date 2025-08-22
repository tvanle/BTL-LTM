package com.crossword.model;

public class Cell {
    private int row;
    private int col;
    private char character;
    private CellType type;
    private CellStatus status;
    private String ownerId;
    private long timestamp;
    
    public Cell() {}
    
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.character = '\0';
        this.type = CellType.EMPTY;
        this.status = CellStatus.EMPTY;
        this.ownerId = null;
        this.timestamp = 0;
    }
    
    public Cell(int row, int col, char character, CellType type) {
        this(row, col);
        this.character = character;
        this.type = type;
        if (type == CellType.LOCKED) {
            this.status = CellStatus.CORRECT;
        }
    }
    
    public enum CellType {
        EMPTY,     // Ô trống - có thể điền
        LOCKED,    // Ô khoá - chứa sẵn ký tự, không thể thay đổi
        BLOCKED    // Ô chặn - không sử dụng
    }
    
    public enum CellStatus {
        EMPTY,      // Ô trống
        PENDING,    // Đang chờ xác nhận từ server
        CORRECT,    // Đã được xác nhận đúng
        INCORRECT   // Không hợp lệ
    }
    
    // Getters and setters
    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }
    
    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }
    
    public char getCharacter() { return character; }
    public void setCharacter(char character) { 
        this.character = character;
        this.timestamp = System.currentTimeMillis();
    }
    
    public CellType getType() { return type; }
    public void setType(CellType type) { this.type = type; }
    
    public CellStatus getStatus() { return status; }
    public void setStatus(CellStatus status) { this.status = status; }
    
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public boolean isEmpty() {
        return character == '\0' && type == CellType.EMPTY;
    }
    
    public boolean isOwnedBy(String playerId) {
        return playerId != null && playerId.equals(ownerId);
    }
}