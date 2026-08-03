package com.bookmyevent.service;

import com.bookmyevent.exception.UserNotFoundException;
import com.bookmyevent.model.User;
import com.bookmyevent.storage.FileStorage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Day 3 — Spring-managed service bean.
 *
 * <p>Before (Day 1):
 * <pre>
 *   UserService userService = new UserService(storage); // manual wiring in App.java
 * </pre>
 *
 * <p>After (Day 3):
 * <pre>
 *   // Spring reads @Service, creates this bean, injects FileStorage automatically.
 *   // No manual `new` anywhere in application code.
 * </pre>
 *
 * <p>Constructor injection is used (not @Autowired on fields) because:
 * <ul>
 *   <li>The {@code fileStorage} field can be {@code final} — guarantees immutability</li>
 *   <li>Missing dependencies cause a clear startup failure</li>
 *   <li>Plain constructor can be called in unit tests without a Spring context</li>
 * </ul>
 */
@Service
public class UserService {

    private final Map<Long, User> usersById = new ConcurrentHashMap<>();
    private final FileStorage fileStorage;
    private long nextId = 1;

    // Spring sees one constructor — injects FileStorage bean automatically.
    public UserService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
        this.usersById.putAll(fileStorage.loadUsers());
        if (!usersById.isEmpty()) {
            nextId = usersById.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
        }
    }

    public User registerUser(String name, String email, String role) {
        User user = new User(nextId++, name, email, role);
        usersById.put(user.getId(), user);
        fileStorage.saveUsers(listUsers()); // save only our own collection
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
