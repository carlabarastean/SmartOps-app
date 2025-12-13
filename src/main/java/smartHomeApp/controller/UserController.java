package smartHomeApp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartHomeApp.model.User;
import smartHomeApp.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }


    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }


    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/login")
    public User loginOrCreate(@RequestBody Map<String, String> payload) {
        String username = payload.getOrDefault("username", "");
        String password = payload.getOrDefault("password", "");
        if (username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        return userService.loginOrRegister(username, password);
    }
}
