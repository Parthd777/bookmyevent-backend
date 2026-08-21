package com.bookmyevent.dto.response;

import com.bookmyevent.model.User;

/** Read-only view of a User returned to the client. Never expose the internal entity directly. */
public class UserResponse {
    private long id;
    private String name;
    private String email;
    private String role;

    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.id = user.getId();
        r.name = user.getName();
        r.email = user.getEmail();
        r.role = user.getRole();
        return r;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
