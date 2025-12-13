package smartHomeApp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartHomeApp.model.Device;
import smartHomeApp.service.DeviceService;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "*")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public List<Device> getAllDevices() {
        return deviceService.findAll();
    }

    @PostMapping
    public Device addDevice(@RequestBody Device device) {
        return deviceService.save(device);
    }

    @DeleteMapping("/{id}")
    public void deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
        return deviceService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Device updateDevice(@PathVariable Long id, @RequestBody Device device) {
        return deviceService.updateDevice(id, device);
    }

    @GetMapping("/room/{roomId}")
    public List<Device> getDevicesByRoom(@PathVariable Long roomId) {
        return deviceService.findDevicesByRoomId(roomId);
    }

    @GetMapping("/active")
    public List<Device> getDevicesByStatus(@RequestParam(defaultValue = "true") boolean status) {
        return deviceService.findActiveDevices(status);
    }
}
