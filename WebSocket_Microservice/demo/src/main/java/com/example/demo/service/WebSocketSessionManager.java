package com.example.demo.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSessionManager {

    private final SimpMessagingTemplate messagingTemplate;
    
    // Map: userId -> sessionId
    private final Map<UUID, String> userSessions = new ConcurrentHashMap<>();

    public WebSocketSessionManager(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void registerSession(UUID userId, String sessionId) {
        userSessions.put(userId, sessionId);
        System.out.println("User " + userId + " connected with session: " + sessionId);
    }

    public void removeSession(UUID userId) {
        userSessions.remove(userId);
        System.out.println("User " + userId + " disconnected");
    }

    public void sendToUser(UUID userId, Object message) {
        String destination = "/topic/alerts/" + userId;
        messagingTemplate.convertAndSend(destination, message);
        System.out.println("Sent message to user " + userId + ": " + message);
    }

    public void sendChatToUser(UUID userId, Object message) {
        String destination = "/topic/chat/user/" + userId;
        messagingTemplate.convertAndSend(destination, message);
        System.out.println("Sent chat message to " + destination + ": " + message);
    }

    public boolean isUserConnected(UUID userId) {
        return userSessions.containsKey(userId);
    }
}
