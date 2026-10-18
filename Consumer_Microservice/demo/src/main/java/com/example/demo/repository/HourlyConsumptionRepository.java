package com.example.demo.repository;

import com.example.demo.entity.HourlyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, Long> {
    Optional<HourlyConsumption> findByDeviceIdAndHourTimestamp(UUID deviceId, LocalDateTime hour);
}
