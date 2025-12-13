package smartHomeApp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private DeviceType type;

    private Boolean status;

    private Double value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnoreProperties({"devices", "user"})
    private Room room;
}
