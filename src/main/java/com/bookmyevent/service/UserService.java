package com.bookmyevent.service;

import com.bookmyevent.exception.UserNotFoundException;
import com.bookmyevent.model.User;
import com.bookmyevent.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User registerUser(String name, String email, String role) {
        User user = new User(name, email, role);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public User findById(long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}