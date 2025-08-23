package com.wordbrain2.websocket.message;

import com.wordbrain2.model.enums.MessageType;
import lombok.Data;

@Data
public class BaseMessage {
    private MessageType type;
    private Object data;
    private long timestamp;
    
    public BaseMessage() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public BaseMessage(MessageType type, Object data) {
        this();
        this.type = type;
        this.data = data;
    }
}