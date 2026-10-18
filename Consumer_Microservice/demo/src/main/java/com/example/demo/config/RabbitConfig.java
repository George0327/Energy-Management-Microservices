package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.queue.name}")
    private String QUEUE_NAME;

    @Value("${app.alert.exchange}")
    private String ALERT_EXCHANGE;

    @Value("${app.alert.routingKey}")
    private String ALERT_ROUTING_KEY;

    @Bean
    public Queue queue() {
        System.out.println("Creating queue: " + QUEUE_NAME);
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Queue alertQueue() {
        return new Queue("device-alert-queue", true);
    }

    @Bean
    public TopicExchange alertExchange() {
        return new TopicExchange(ALERT_EXCHANGE);
    }

    @Bean
    public Binding alertBinding(Queue alertQueue, TopicExchange alertExchange) {
        return BindingBuilder.bind(alertQueue).to(alertExchange).with(ALERT_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        System.out.println(" [RabbitConfig] Creating RabbitTemplate with connection: "
                + connectionFactory.getHost());
        return new RabbitTemplate(connectionFactory);
    }
}