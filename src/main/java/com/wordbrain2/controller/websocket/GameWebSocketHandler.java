package com.wordbrain2.controller.websocket;

import com.google.gson.Gson;
import com.wordbrain2.model.enums.MessageType;
import com.wordbrain2.websocket.message.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    
    @Autowired
    private MessageRouter messageRouter;
    
    @Autowired
    private ConnectionManager connectionManager;
    
    private final Gson gson = new Gson();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        
        // Register session with connection manager
        connectionManager.addSession(session.getId(), session);
        
        // Send connection success message
        BaseMessage welcomeMessage = new BaseMessage();
        welcomeMessage.setType(MessageType.CONNECTION_SUCCESS.name());
        Map<String, Object> welcomeData = new HashMap<>();
        welcomeData.put("sessionId", session.getId());
        welcomeData.put("message", "Connected to game server");
        welcomeMessage.setData(welcomeData);
        
        session.sendMessage(new TextMessage(gson.toJson(welcomeMessage)));
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Delegate all message handling to MessageRouter
        messageRouter.routeMessage(session, message);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {} - {}", session.getId(), status);
        
        // Notify router about disconnect
        messageRouter.handleDisconnect(session.getId());
        
        // Remove from connection manager
        connectionManager.removeSession(session.getId());
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Transport error for session {}: {}", session.getId(), exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
    }
}