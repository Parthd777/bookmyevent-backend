package com.bookmyevent.dto.response;

public class LoginResponse {

    private final String token;
    private final String tokenType = "Bearer";
    private final String email;
    private final String role;
    private final long expiresInMs;

    public LoginResponse(String token, String email, String role, long expiresInMs) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.expiresInMs = expiresInMs;
    }

    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public long getExpiresInMs() { return expiresInMs; }
}
