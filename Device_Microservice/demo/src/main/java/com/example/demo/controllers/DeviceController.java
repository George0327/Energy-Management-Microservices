package com.example.demo.controllers;

import com.example.demo.entities.Device;
import com.example.demo.services.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<Device> createDevice(@RequestBody Device device) {
        Device createdDevice = deviceService.createDevice(device);
        return ResponseEntity.ok(createdDevice);
    }

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        List<Device> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable UUID id) {
        Device device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(device);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable UUID id, @RequestBody Device device) {
        Device updatedDevice = deviceService.updateDevice(id, device);
        return ResponseEntity.ok(updatedDevice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Device> deleteDevice(@PathVariable UUID id) {
        Device deletedDevice = deviceService.deleteDevice(id);
        return ResponseEntity.ok(deletedDevice);
    }

    @PutMapping("/{deviceId}/assign/{userId}")
    public ResponseEntity<Device> assignDeviceToUser(
            @PathVariable UUID deviceId,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(deviceService.assignUser(deviceId, userId));
    }

    @PutMapping("/{deviceId}/unassign")
    public ResponseEntity<Device> unassignDevice(@PathVariable UUID deviceId) {
        return ResponseEntity.ok(deviceService.unassignUser(deviceId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Device>> getDevicesByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(deviceService.getDevicesByUser(userId));
    }
}
