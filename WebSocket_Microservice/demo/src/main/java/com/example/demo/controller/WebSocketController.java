package com.example.demo.controller;

import com.example.demo.service.WebSocketSessionManager;
import com.example.demo.util.JwtUtil;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class WebSocketController {

    private final WebSocketSessionManager sessionManager;
    private final JwtUtil jwtUtil;

    public WebSocketController(WebSocketSessionManager sessionManager, JwtUtil jwtUtil) {
        this.sessionManager = sessionManager;
        this.jwtUtil = jwtUtil;
    }

    @MessageMapping("/register")
    public void registerClient(StompHeaderAccessor headerAccessor, @Header("Authorization") String token) {
        try {
            String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            
            if (jwtUtil.validateToken(jwtToken)) {
                UUID userId = jwtUtil.extractUserId(jwtToken);
                String sessionId = headerAccessor.getSessionId();
                
                sessionManager.registerSession(userId, sessionId);
            }
        } catch (Exception e) {
            System.err.println("Error registering client: " + e.getMessage());
        }
    }
}
