package com.bookmyevent.controller;

import com.bookmyevent.dto.request.LoginRequest;
import com.bookmyevent.dto.response.LoginResponse;
import com.bookmyevent.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * POST /auth/login
     * Verifies email + password and returns a signed JWT.
     * Returns 401 when credentials are invalid (handled by GlobalExceptionHandler).
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String role = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .findFirst()
                .orElse("ROLE_CUSTOMER")
                .replaceFirst("^ROLE_", "");

        String token = jwtService.generateToken(authentication.getName(), role);
        return ResponseEntity.ok(new LoginResponse(
                token, authentication.getName(), role, jwtService.getExpirationMillis()));
    }
}
