package com.bookmyevent.service;

import com.bookmyevent.exception.UserNotFoundException;
import com.bookmyevent.model.User;
import com.bookmyevent.storage.FileStorage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final Map<Long, User> usersById = new ConcurrentHashMap<>();
    private final FileStorage fileStorage;
    private long nextId = 1;

    public UserService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
        this.usersById.putAll(fileStorage.loadUsers());
        if (!usersById.isEmpty()) {
            nextId = usersById.keySet().stream().max(Comparator.naturalOrder()).orElse(0L) + 1;
        }
    }

    public User registerUser(String name, String email, String role) {
        User user = new User(nextId++, name, email, role);
        usersById.put(user.getId(), user);
        fileStorage.saveUsers(listUsers());
        return user;
    }

    public User getUserById(long id) {
        User user = usersById.get(id);
        if (user == null) throw new UserNotFoundException("User not found with id " + id);
        return user;
    }

    public List<User> listUsers() {
        return new ArrayList<>(usersById.values());
    }
}
