package smartHomeApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smartHomeApp.model.TemperatureReading;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TemperatureReadingRepository extends JpaRepository<TemperatureReading, Long> {

    List<TemperatureReading> findByDeviceIdOrderByTimestampDesc(Long deviceId);

    @Query("SELECT tr FROM TemperatureReading tr WHERE tr.deviceId = :deviceId AND tr.timestamp >= :startTime ORDER BY tr.timestamp DESC")
    List<TemperatureReading> findByDeviceIdAndTimestampAfterOrderByTimestampDesc(
        @Param("deviceId") Long deviceId,
        @Param("startTime") LocalDateTime startTime
    );

    @Query("SELECT tr FROM TemperatureReading tr WHERE tr.timestamp >= :startTime ORDER BY tr.timestamp DESC")
    List<TemperatureReading> findByTimestampAfterOrderByTimestampDesc(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT tr FROM TemperatureReading tr ORDER BY tr.timestamp DESC LIMIT 100")
    List<TemperatureReading> findLast100Readings();
}
