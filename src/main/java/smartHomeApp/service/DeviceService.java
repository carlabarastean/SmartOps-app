package smartHomeApp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartHomeApp.model.Device;
import smartHomeApp.repository.DeviceRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<Device> findAll() {
        return deviceRepository.findAll();
    }

    public Optional<Device> findById(Long id) {
        return deviceRepository.findById(id);
    }

    public Device save(Device device) {
        return deviceRepository.save(device);
    }

    public Device updateDevice(Long id, Device updatedDevice) {
        Device existing = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + id));
        existing.setName(updatedDevice.getName());
        existing.setType(updatedDevice.getType());
        existing.setStatus(updatedDevice.getStatus());
        existing.setValue(updatedDevice.getValue());
        existing.setRoom(updatedDevice.getRoom());
        return deviceRepository.save(existing);
    }

    @Transactional
    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + id));
        if (Boolean.TRUE.equals(device.getStatus())) {
            throw new IllegalStateException("Cannot delete an active device (id=" + id + ").");
        }
        deviceRepository.delete(device);
    }

    public List<Device> findDevicesByRoomId(Long roomId) {
        return deviceRepository.findDevicesByRoomId(roomId);
    }

    public List<Device> findActiveDevices(Boolean status) {
        return deviceRepository.findActiveDevices(status);
    }
}
