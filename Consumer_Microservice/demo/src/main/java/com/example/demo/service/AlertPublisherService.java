package com.example.demo.service;

import com.example.demo.dto.ConsumptionAlertDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AlertPublisherService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.alert.exchange:alert-exchange}")
    private String alertExchange;

    @Value("${app.alert.routingKey:device.alert}")
    private String alertRoutingKey;

    public AlertPublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public void publishAlert(ConsumptionAlertDTO alert) {
        try {
            String json = objectMapper.writeValueAsString(alert);
            rabbitTemplate.convertAndSend(alertExchange, alertRoutingKey, json);
            System.out.println("Alert published: " + alert);
        } catch (Exception e) {
            System.err.println("Error publishing alert: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
