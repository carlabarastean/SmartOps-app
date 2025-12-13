package smartHomeApp.service;

import org.springframework.stereotype.Service;
import smartHomeApp.model.TemperatureReading;
import smartHomeApp.repository.TemperatureReadingRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TemperatureService {

    private final TemperatureReadingRepository temperatureReadingRepository;

    public TemperatureService(TemperatureReadingRepository temperatureReadingRepository) {
        this.temperatureReadingRepository = temperatureReadingRepository;
    }

    public TemperatureReading saveReading(Long deviceId, Double temperature, String roomName) {
        TemperatureReading reading = TemperatureReading.builder()
                .deviceId(deviceId)
                .temperature(temperature)
                .timestamp(LocalDateTime.now())
                .roomName(roomName)
                .build();
        return temperatureReadingRepository.save(reading);
    }

    public List<TemperatureReading> getReadingsByDevice(Long deviceId) {
        return temperatureReadingRepository.findByDeviceIdOrderByTimestampDesc(deviceId);
    }

    public List<TemperatureReading> getRecentReadings(Long deviceId, int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        return temperatureReadingRepository.findByDeviceIdAndTimestampAfterOrderByTimestampDesc(deviceId, startTime);
    }

    public List<TemperatureReading> getAllRecentReadings(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        return temperatureReadingRepository.findByTimestampAfterOrderByTimestampDesc(startTime);
    }

    public List<TemperatureReading> getLast100Readings() {
        return temperatureReadingRepository.findLast100Readings();
    }

    public void generateSampleData(Long deviceId, String roomName) {
        LocalDateTime now = LocalDateTime.now();


        for (int i = 0; i < 96; i++) {
            LocalDateTime timestamp = now.minusMinutes(30L * i);


            double baseTemp = 22.0;
            double hourOfDay = timestamp.getHour();


            double dailyCycle = Math.sin((hourOfDay - 6) * Math.PI / 12) * 3;


            double randomVariation = (Math.random() - 0.5) * 2;


            double heatingCycle = Math.sin(i * 0.1) * 1;

            double finalTemp = baseTemp + dailyCycle + randomVariation + heatingCycle;


            finalTemp = Math.round(finalTemp * 10.0) / 10.0;

            TemperatureReading reading = TemperatureReading.builder()
                    .deviceId(deviceId)
                    .temperature(finalTemp)
                    .timestamp(timestamp)
                    .roomName(roomName)
                    .build();

            temperatureReadingRepository.save(reading);
        }
    }
}
