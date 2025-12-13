package smartHomeApp.service;

import org.springframework.stereotype.Service;
import smartHomeApp.model.User;
import smartHomeApp.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User loginOrRegister(String username, String password) {
        return userRepository.findByUsername(username)
                .map(existing -> existing)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(username)
                            .email(username + "@example.com")
                            .password(password)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    public User updateUser(Long id, User user) {
        if (userRepository.existsById(id)) {
            user.setId(id);
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found with id " + id);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
