package com.bookmyevent.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public class CreateUserRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a valid email address")
    private String email;

    // Honoured only for callers with ROLE_ADMIN; anonymous signups are always CUSTOMER.
    @Pattern(regexp = "ADMIN|CUSTOMER", message = "role must be ADMIN or CUSTOMER")
    private String role;

    @NotBlank(message = "password must not be blank")
    @Size(min = 8, message = "password must contain at least 8 characters")
    private String password;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
