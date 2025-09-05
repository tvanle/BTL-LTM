package com.wordbrain2.controller.websocket;

import com.google.gson.Gson;
import com.wordbrain2.model.enums.MessageType;
import com.wordbrain2.websocket.message.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class GameWebSocketHandler {
    
    @Autowired
    private MessageRouter messageRouter;
    
    @Autowired
    private ConnectionManager connectionManager;
    
    private final Gson gson = new Gson();
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private final Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private volatile boolean running = false;
    private Thread serverThread;
    
    @PostConstruct
    public void init() {
        startTcpServer();
    }
    
    @PreDestroy
    public void destroy() {
        stopTcpServer();
    }
    
    private void startTcpServer() {
        executorService = Executors.newCachedThreadPool();
        running = true;
        
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(5555);
                log.info("TCP Server started on port 5555");
                
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        String sessionId = "tcp_" + System.currentTimeMillis() + "_" + clientSocket.getPort();
                        
                        ClientHandler clientHandler = new ClientHandler(clientSocket, sessionId);
                        clientHandlers.put(sessionId, clientHandler);
                        executorService.execute(clientHandler);
                        
                    } catch (IOException e) {
                        if (running) {
                            log.error("Error accepting client connection", e);
                        }
                    }
                }
            } catch (IOException e) {
                log.error("Failed to start TCP server", e);
            }
        });
        serverThread.start();
    }
    
    private void stopTcpServer() {
        running = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("Error closing server socket", e);
        }
        
        clientHandlers.values().forEach(ClientHandler::close);
        clientHandlers.clear();
        
        if (executorService != null) {
            executorService.shutdownNow();
        }
        
        if (serverThread != null) {
            try {
                serverThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public void sendMessage(String sessionId, String message) {
        ClientHandler handler = clientHandlers.get(sessionId);
        if (handler != null) {
            handler.sendMessage(message);
        }
    }
    
    public void broadcastMessage(String message) {
        clientHandlers.values().forEach(handler -> handler.sendMessage(message));
    }
    
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final String sessionId;
        private DataInputStream input;
        private DataOutputStream output;
        private volatile boolean active = true;
        
        public ClientHandler(Socket socket, String sessionId) {
            this.socket = socket;
            this.sessionId = sessionId;
            
            try {
                this.input = new DataInputStream(socket.getInputStream());
                this.output = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                log.error("Error setting up client streams", e);
                close();
            }
        }
        
        @Override
        public void run() {
            try {
                log.info("TCP connection established: {}", sessionId);
                
                // Register session with connection manager
                connectionManager.addTcpSession(sessionId, this);
                
                // Send connection success message
                BaseMessage welcomeMessage = new BaseMessage();
                welcomeMessage.setMessageType(MessageType.CONNECTION_SUCCESS);
                Map<String, Object> welcomeData = new HashMap<>();
                welcomeData.put("sessionId", sessionId);
                welcomeData.put("message", "Connected to game server");
                welcomeMessage.setData(welcomeData);
                
                sendMessage(gson.toJson(welcomeMessage));
                
                // Read messages from client
                while (active && !socket.isClosed()) {
                    try {
                        String message = input.readUTF();
                        if (message != null && !message.isEmpty()) {
                            // Route message to MessageRouter
                            messageRouter.routeTcpMessage(sessionId, message);
                        }
                    } catch (EOFException e) {
                        // Client disconnected normally
                        break;
                    } catch (SocketTimeoutException e) {
                        // Timeout, continue loop
                    } catch (IOException e) {
                        if (active) {
                            log.error("Error reading from client {}", sessionId, e);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error in client handler for {}", sessionId, e);
            } finally {
                close();
            }
        }
        
        public void sendMessage(String message) {
            if (active && output != null) {
                try {
                    output.writeUTF(message);
                    output.flush();
                } catch (IOException e) {
                    log.error("Error sending message to client {}", sessionId, e);
                    close();
                }
            }
        }
        
        public void close() {
            if (!active) return;
            active = false;
            
            log.info("TCP connection closed: {}", sessionId);
            
            // Notify router about disconnect
            messageRouter.handleDisconnect(sessionId);
            
            // Remove from connection manager
            connectionManager.removeSession(sessionId);
            clientHandlers.remove(sessionId);
            
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                log.error("Error closing client resources", e);
            }
        }
    }
}