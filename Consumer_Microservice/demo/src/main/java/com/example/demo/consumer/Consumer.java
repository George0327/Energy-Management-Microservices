package com.example.demo.consumer;

import com.example.demo.dto.DeviceMeasurementDTO;
import com.example.demo.service.AggregationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {

    private final AggregationService aggregationService;

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules();

    public Consumer(AggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @RabbitListener(queues = "${app.queue.name}")
    public void receive(byte[] messageBody) {
        try {
            String json = new String(messageBody);
            //System.out.println("Raw message: " + json);

            DeviceMeasurementDTO dto = mapper.readValue(json, DeviceMeasurementDTO.class);

//            System.out.println("Parsed DTO:");
//            System.out.println("timestamp = " + dto.getTimestamp());
//            System.out.println("deviceId  = " + dto.getDeviceId());
//            System.out.println("value     = " + dto.getMeasurementValue());
            aggregationService.processMeasurement(dto);
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
