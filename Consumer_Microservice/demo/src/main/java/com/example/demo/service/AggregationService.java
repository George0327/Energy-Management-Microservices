package com.example.demo.service;

import com.example.demo.dto.ConsumptionAlertDTO;
import com.example.demo.dto.DeviceMeasurementDTO;
import com.example.demo.entity.HourlyConsumption;
import com.example.demo.repository.HourlyConsumptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;

@Service
public class AggregationService {

    private final HourlyConsumptionRepository repo;
    private final AlertPublisherService alertPublisher;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${device.service.url}")
    private String deviceServiceUrl;

    public AggregationService(HourlyConsumptionRepository repo,
                            AlertPublisherService alertPublisher) {
        this.repo = repo;
        this.alertPublisher = alertPublisher;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    public void processMeasurement(DeviceMeasurementDTO dto) {

        UUID deviceId = dto.getDeviceId();
        LocalDateTime hour = dto.getTimestamp();
        Double value = dto.getMeasurementValue();

        HourlyConsumption hc =
                repo.findByDeviceIdAndHourTimestamp(deviceId, hour)
                        .orElseGet(() -> {
                            HourlyConsumption newRecord = new HourlyConsumption();
                            newRecord.setDeviceId(deviceId);
                            newRecord.setHourTimestamp(hour);
                            newRecord.setTotalKwh(0.0);
                            return newRecord;
                        });

        hc.setTotalKwh(hc.getTotalKwh() + value);

//        System.out.println("Updated hourly consumption: " +
//                hc.getDeviceId() + " | " +
//                hc.getHourTimestamp() + " | total = " + hc.getTotalKwh());

        repo.save(hc);

        // Check if consumption exceeds limit and publish alert
        checkAndPublishAlert(deviceId, hc.getTotalKwh(), hour);
    }

    private void checkAndPublishAlert(UUID deviceUuid, Double totalKwh, LocalDateTime timestamp) {
        try {
            String url = deviceServiceUrl + "/" + deviceUuid;
            Map<String, Object> device = restTemplate.getForObject(url, Map.class);

            System.out.println("Fetched device info: " + device);
            
            if (device != null) {
                Double maxConsValue = extractDouble(device, "maxConsValue");
                Object userIdObj = device.get("userId");
                UUID userId = userIdObj != null ? UUID.fromString(userIdObj.toString()) : null;
                
                if (maxConsValue != null && userId != null && totalKwh > maxConsValue) {
                    ConsumptionAlertDTO alert = new ConsumptionAlertDTO(
                        deviceUuid,
                        userId,
                        totalKwh,
                        maxConsValue,
                        timestamp
                    );
                    alertPublisher.publishAlert(alert);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching device from microservice: " + e.getMessage());
        }
    }

    private Double extractDouble(Map<String, Object> obj, String key) {
        Object value = obj.get(key);
        if (value != null) {
            if (value instanceof Double) return (Double) value;
            if (value instanceof Number) return ((Number) value).doubleValue();
        }
        return null;
    }

}
