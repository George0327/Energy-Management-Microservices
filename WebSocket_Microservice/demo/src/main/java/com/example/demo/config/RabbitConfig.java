package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.alert.queue}")
    private String alertQueueName;

    @Value("${app.chat.exchange}")
    private String chatExchangeName;

    @Value("${app.chat.queue}")
    private String chatQueueName;

    @Value("${app.chat.routing-key}")
    private String chatRoutingKey;

    @Bean
    public Queue alertQueue() {
        return new Queue(alertQueueName, true);
    }

    // Chat exchange (must match Customer Service MS)
    @Bean
    public TopicExchange chatExchange() {
        System.out.println("Creating chat exchange: " + chatExchangeName);
        return new TopicExchange(chatExchangeName, true, false);
    }

    // Chat queue for WebSocket service
    @Bean
    public Queue chatQueue() {
        System.out.println("Creating chat queue: " + chatQueueName);
        return new Queue(chatQueueName, true);
    }

    // Bind chat queue to exchange with routing key pattern
    @Bean
    public Binding chatBinding(Queue chatQueue, TopicExchange chatExchange) {
        System.out.println("Binding " + chatQueueName + " to " + chatExchangeName + " with key " + chatRoutingKey);
        return BindingBuilder.bind(chatQueue).to(chatExchange).with(chatRoutingKey);
    }
}
