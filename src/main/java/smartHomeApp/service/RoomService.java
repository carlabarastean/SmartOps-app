package smartHomeApp.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import smartHomeApp.model.Room;
import smartHomeApp.model.User;
import smartHomeApp.repository.RoomRepository;
import smartHomeApp.repository.UserRepository;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public RoomService(RoomRepository roomRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Room createRoom(String name, Integer floor, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Room owner is required");
        }
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Room room = Room.builder()
                .name(name)
                .floor(floor)
                .user(owner)
                .build();
        return roomRepository.save(room);
    }

    public Room updateRoom(Long roomId, String name, Integer floor, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found: " + roomId));

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        room.setName(name);
        room.setFloor(floor);
        room.setUser(owner);
        return roomRepository.save(room);
    }

    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found: " + roomId));

        // Check if room has devices
        if (room.getDevices() != null && !room.getDevices().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete room with active devices");
        }

        roomRepository.delete(room);
    }
}

