package com.example.demo.consumer;

import com.example.demo.dto.ConsumptionAlertDTO;
import com.example.demo.service.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AlertConsumer {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public AlertConsumer(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @RabbitListener(queues = "${app.alert.queue}")
    public void handleAlert(String message) {
        try {
            System.out.println("Received alert from RabbitMQ: " + message);
            
            ConsumptionAlertDTO alert = objectMapper.readValue(message, ConsumptionAlertDTO.class);
            
            // Prepare WebSocket message
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", "CONSUMPTION_ALERT");
            wsMessage.put("deviceId", alert.getDeviceId().toString());
            wsMessage.put("consumption", alert.getConsumption());
            wsMessage.put("maxConsumption", alert.getMaxConsumption());
            wsMessage.put("timestamp", alert.getTimestamp().toString());
            wsMessage.put("message", "Device consumption exceeded limit!");
            
            // Send to user's WebSocket session
            if (alert.getUserId() != null) {
                sessionManager.sendToUser(alert.getUserId(), wsMessage);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing alert: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
