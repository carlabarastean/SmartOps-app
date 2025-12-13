package smartHomeApp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer floor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"rooms", "password", "hibernateLazyInitializer", "handler"})
    private User user;

    @OneToMany(mappedBy = "room")
    @JsonIgnoreProperties({"room"})
    private Set<Device> devices;
}
