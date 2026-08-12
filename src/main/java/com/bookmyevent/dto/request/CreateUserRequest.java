package com.bookmyevent.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Day 4 — Why DTOs instead of exposing entities directly?
 *
 * <ul>
 *   <li>Entities may have fields the client should never set (e.g. {@code id}, {@code createdAt})</li>
 *   <li>DTOs decouple the API contract from the internal data model — entities can change without breaking clients</li>
 *   <li>Validation annotations belong on DTOs, not entities (entities are persistence-layer concerns)</li>
 * </ul>
 */
public class CreateUserRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a valid email address")
    private String email;

    @NotBlank(message = "role must not be blank")
    @Pattern(regexp = "ADMIN|CUSTOMER", message = "role must be ADMIN or CUSTOMER")
    private String role;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
