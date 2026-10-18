package com.example.demo.services;

import com.example.demo.entities.Device;
import com.example.demo.repositories.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Device createDevice(Device device) {
        return deviceRepository.save(device);
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Device getDeviceById(UUID id) {
        return deviceRepository.findById(id).orElse(null);
    }

    public Device updateDevice(UUID id, Device newDevice) {
        Device existingDevice = deviceRepository.findById(id).orElse(null);
        if (existingDevice != null) {
            existingDevice.setName(newDevice.getName());
            existingDevice.setMaxConsValue(newDevice.getMaxConsValue());
            return deviceRepository.save(existingDevice);
        }
        return null;
    }

    public Device deleteDevice(UUID id) {
        Device existingDevice = deviceRepository.findById(id).orElse(null);
        if (existingDevice != null) {
            deviceRepository.deleteById(id);
            return existingDevice;
        }
        return null;
    }

    public Device assignUser(UUID deviceId, UUID userId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
        device.setUserId(userId != null ? userId : null);
        return deviceRepository.save(device);
    }

    public Device unassignUser(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        device.setUserId(null);
        return deviceRepository.save(device);
    }

    public List<Device> getDevicesByUser(UUID userId) {
        return deviceRepository.findByUserId(userId);
    }

}
