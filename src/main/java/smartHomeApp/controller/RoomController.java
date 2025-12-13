package smartHomeApp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartHomeApp.model.Room;
import smartHomeApp.service.RoomService;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> getRooms() {
        return roomService.findAll();
    }

    @PostMapping
    public Room createRoom(@RequestBody Room room) {
        if (room.getUser() == null || room.getUser().getId() == null) {
            throw new IllegalArgumentException("Room owner is required");
        }
        return roomService.createRoom(room.getName(), room.getFloor(), room.getUser().getId());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        if (room.getUser() == null || room.getUser().getId() == null) {
            throw new IllegalArgumentException("Room owner is required");
        }
        Room updated = roomService.updateRoom(id, room.getName(), room.getFloor(), room.getUser().getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}

