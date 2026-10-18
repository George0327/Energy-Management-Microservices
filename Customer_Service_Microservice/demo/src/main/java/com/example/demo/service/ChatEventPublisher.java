package com.example.demo.service;

import com.example.demo.dto.ChatEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class ChatEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Value("${app.chat.exchange}")
    private String chatExchange;

    public ChatEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishChatEvent(ChatEventDTO event) {
        String routingKey = "chat.user." + event.getUserId();
        
        try {
            String json = objectMapper.writeValueAsString(event);
            
            MessageProperties props = new MessageProperties();
            props.setContentType("application/json");
            
            Message message = new Message(json.getBytes(StandardCharsets.UTF_8), props);
            
            rabbitTemplate.send(chatExchange, routingKey, message);
            
            System.out.println(" [ChatEventPublisher] Sent JSON: " + json + " to " + chatExchange + " with routing key " + routingKey);
        } catch (Exception e) {
            System.err.println(" [ChatEventPublisher] Error publishing chat event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
