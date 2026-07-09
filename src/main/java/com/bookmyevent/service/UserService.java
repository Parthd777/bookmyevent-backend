package com.bookmyevent.service;

import com.bookmyevent.exception.UserNotFoundException;
import com.bookmyevent.model.User;
import com.bookmyevent.storage.FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final Map<Long, User> usersById = new ConcurrentHashMap<>();
    private final FileStorage storage;
    private long nextId = 1;

    public UserService(FileStorage storage) {
        this.storage = storage;
        this.usersById.putAll(storage.loadUsers());
        if (!usersById.isEmpty()) {
            nextId = usersById.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
        }
    }

    public User registerUser(String name, String email, String role) {
        User user = new User(nextId++, name, email, role);
        usersById.put(user.getId(), user);
        storage.saveAll(listUsers(), new ArrayList<>(storage.loadVenues().values()), new ArrayList<>(storage.loadEvents().values()), new ArrayList<>(storage.loadBookings().values()));
        return user;
    }

    public User getUserById(long id) {
        User user = usersById.get(id);
        if (user == null) {
            throw new UserNotFoundException("User not found with id " + id);
        }
        return user;
    }

    public List<User> listUsers() {
        return new ArrayList<>(usersById.values());
    }
}
