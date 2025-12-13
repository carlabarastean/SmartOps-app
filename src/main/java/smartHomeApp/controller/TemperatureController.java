package smartHomeApp.controller;

import org.springframework.web.bind.annotation.*;
import smartHomeApp.model.TemperatureReading;
import smartHomeApp.service.TemperatureService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/temperature")
@CrossOrigin(origins = "*")
public class TemperatureController {

    private final TemperatureService temperatureService;

    public TemperatureController(TemperatureService temperatureService) {
        this.temperatureService = temperatureService;
    }

    @GetMapping("/device/{deviceId}")
    public List<TemperatureReading> getTemperatureReadings(@PathVariable Long deviceId) {
        return temperatureService.getReadingsByDevice(deviceId);
    }

    @GetMapping("/device/{deviceId}/recent")
    public List<TemperatureReading> getRecentReadings(
            @PathVariable Long deviceId,
            @RequestParam(defaultValue = "24") int hours) {
        return temperatureService.getRecentReadings(deviceId, hours);
    }

    @GetMapping("/recent")
    public List<TemperatureReading> getAllRecentReadings(
            @RequestParam(defaultValue = "24") int hours) {
        return temperatureService.getAllRecentReadings(hours);
    }

    @GetMapping("/trend")
    public List<TemperatureReading> getTemperatureTrend() {
        return temperatureService.getLast100Readings();
    }

    @PostMapping("/device/{deviceId}/reading")
    public TemperatureReading addReading(
            @PathVariable Long deviceId,
            @RequestBody Map<String, Object> payload) {
        Double temperature = Double.valueOf(payload.get("temperature").toString());
        String roomName = payload.get("roomName").toString();
        return temperatureService.saveReading(deviceId, temperature, roomName);
    }

    @PostMapping("/device/{deviceId}/generate-sample-data")
    public String generateSampleData(@PathVariable Long deviceId, @RequestParam String roomName) {
        temperatureService.generateSampleData(deviceId, roomName);
        return "Sample temperature data generated successfully for device " + deviceId;
    }
}
