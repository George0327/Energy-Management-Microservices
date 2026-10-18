package com.example.demo.consumer;

import com.example.demo.dto.ChatEventDTO;
import com.example.demo.service.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ChatConsumer {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public ChatConsumer(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @RabbitListener(queues = "${app.chat.queue}")
    public void handleChatEvent(String message) {
        try {
            System.out.println(" [ChatConsumer] Received chat event from RabbitMQ: " + message);
            
            ChatEventDTO chatEvent = objectMapper.readValue(message, ChatEventDTO.class);
            
            // Prepare WebSocket message
            Map<String, Object> wsMessage = getStringObjectMap(chatEvent);

            // Send to user's chat topic
            sessionManager.sendChatToUser(chatEvent.getUserId(), wsMessage);
            
            System.out.println(" [ChatConsumer] Sent chat message to user " + chatEvent.getUserId());
            
        } catch (Exception e) {
            System.err.println(" [ChatConsumer] Error processing chat event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Map<String, Object> getStringObjectMap(ChatEventDTO chatEvent) {
        Map<String, Object> wsMessage = new HashMap<>();
        wsMessage.put("type", "CHAT_MESSAGE");
        wsMessage.put("messageId", chatEvent.getMessageId().toString());
        wsMessage.put("userId", chatEvent.getUserId().toString());
        wsMessage.put("senderId", chatEvent.getSenderId().toString());
        wsMessage.put("senderType", chatEvent.getSenderType());
        wsMessage.put("content", chatEvent.getContent());
        wsMessage.put("timestamp", chatEvent.getTimestamp().toString());
        return wsMessage;
    }
}
