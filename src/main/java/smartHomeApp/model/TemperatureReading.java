package smartHomeApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "temperature_readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemperatureReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long deviceId;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column
    private String roomName;
}
