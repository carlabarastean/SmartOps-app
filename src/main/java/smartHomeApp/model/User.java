package smartHomeApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    @JsonIgnore
    private String password;

    @Column(name = "preferred_temperature")
    private Double preferredTemperature;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private Set<Room> rooms;
}
