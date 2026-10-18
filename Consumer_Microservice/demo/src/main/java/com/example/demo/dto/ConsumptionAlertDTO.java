package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConsumptionAlertDTO {
    private UUID deviceId;
    private UUID userId;
    private Double consumption;
    private Double maxConsumption;
    private LocalDateTime timestamp;

    @Override
    public String toString() {
        return "ConsumptionAlertDTO{" +
                "deviceId=" + deviceId +
                ", userId=" + userId +
                ", consumption=" + consumption +
                ", maxConsumption=" + maxConsumption +
                ", timestamp=" + timestamp +
                '}';
    }
}
