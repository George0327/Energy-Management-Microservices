package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ConsumptionAlertDTO {
    private UUID deviceId;
    private UUID userId;
    private Double consumption;
    private Double maxConsumption;
    private LocalDateTime timestamp;
}
