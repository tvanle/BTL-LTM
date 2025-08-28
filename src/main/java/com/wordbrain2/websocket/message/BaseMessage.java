package com.wordbrain2.websocket.message;

import com.wordbrain2.model.enums.MessageType;
import lombok.Data;
import java.util.Map;

@Data
public class BaseMessage {
    private String type;  // Change to String to accept both enum names and strings
    private Map<String, Object> data;
    private long timestamp;
    
    public BaseMessage() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public BaseMessage(String type, Map<String, Object> data) {
        this();
        this.type = type;
        this.data = data;
    }
    
    public BaseMessage(MessageType type, Map<String, Object> data) {
        this();
        this.type = type.name();
        this.data = data;
    }
    
    public MessageType getMessageType() {
        if (type == null) {
            return null;
        }
        try {
            return MessageType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public void setMessageType(MessageType messageType) {
        this.type = messageType != null ? messageType.name() : null;
    }
}