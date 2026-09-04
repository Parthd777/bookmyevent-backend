package com.bookmyevent.controller;

import com.bookmyevent.dto.request.CreateUserRequest;
import com.bookmyevent.dto.response.UserResponse;
import com.bookmyevent.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Day 4 — REST controller for User operations.
 *
 * <p>Day 3 equivalent: menu option 1 in ConsoleRunner.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>{@code @RestController} = {@code @Controller} + {@code @ResponseBody} —
 *       return values are serialised to JSON automatically by Jackson</li>
 *   <li>{@code @Valid} triggers the Bean Validation annotations on the request DTO;
 *       failures are caught by {@link com.bookmyevent.exception.GlobalExceptionHandler}</li>
 *   <li>The controller never touches the entity model directly — it works with DTOs only</li>
 * </ul>
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /users
     * Register a new user.
     * Returns 201 Created with the created user body.
     */
    @PostMapping
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody CreateUserRequest request, Authentication authentication) {

        boolean callerIsAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        // Anonymous callers cannot choose their own role, otherwise anyone could self-grant ADMIN.
        String role = (callerIsAdmin && request.getRole() != null)
                ? request.getRole().toUpperCase()
                : "CUSTOMER";

        var user = userService.registerUser(
                request.getName(),
                request.getEmail(),
                role,
                request.getPassword()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    /**
     * GET /users/{id}
     * Fetch a single user by id.
     * Returns 404 if not found (handled by GlobalExceptionHandler).
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long id) {
        return ResponseEntity.ok(UserResponse.from(userService.findById(id)));
    }

    /**
     * GET /users
     * List all registered users.
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> response = userService.findAll().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
