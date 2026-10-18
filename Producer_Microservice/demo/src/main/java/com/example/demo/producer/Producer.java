package com.example.demo.producer;

import com.example.demo.dto.DeviceMeasurementDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

@Component
public class Producer {

    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Value("${app.queue.name}")
    private String queueName;

    // ID of the device the simulator reports for (set DEVICE_ID to the ID of a device created in the app).
    @Value("${app.device.id:e863b111-a85a-45cf-86fc-e7d4fb4978e6}")
    private UUID deviceId;

    public Producer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedRate = 5000)
    public void sendMeasurement() {
        try {
            double value = 40 + (60 * random.nextDouble()); // simulate 0.1–0.7 kWh

            DeviceMeasurementDTO dto = new DeviceMeasurementDTO(
                    LocalDateTime.now(),
                    deviceId,
                    value
            );

            String json = mapper.writeValueAsString(dto);

            MessageProperties props = new MessageProperties();
            props.setContentType("application/json");

            Message message = new Message(json.getBytes(StandardCharsets.UTF_8), props);

            rabbitTemplate.send(queueName, message);

            System.out.println(" Producer Sent JSON: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
