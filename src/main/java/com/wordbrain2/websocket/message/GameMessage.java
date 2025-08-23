package com.wordbrain2.websocket.message;

import com.wordbrain2.model.enums.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GameMessage extends BaseMessage {
    private String roomCode;
    private String playerId;
    
    public GameMessage() {
        super();
    }
    
    public GameMessage(MessageType type, Object data) {
        super(type, data);
    }
}