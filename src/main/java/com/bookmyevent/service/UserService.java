package com.bookmyevent.service;

import com.bookmyevent.exception.BookingNotAllowedException;
import com.bookmyevent.exception.UserNotFoundException;
import com.bookmyevent.model.User;
import com.bookmyevent.model.UserRole;
import com.bookmyevent.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public User registerUser(String name, String email, String role, String password) {
        // Duplicate emails would make the login identity ambiguous.
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BookingNotAllowedException("An account with this email already exists.");
        }
        User user = new User(name, email, UserRole.valueOf(role.toUpperCase()));
        user.setPasswordHash(passwordEncoder.encode(password));
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