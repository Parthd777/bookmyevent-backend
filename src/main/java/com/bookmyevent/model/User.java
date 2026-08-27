package com.bookmyevent.model;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    private String name;
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column
    private String passwordHash;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<Event> organizedEvents = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    public User() {}
    public User(String name, String email, UserRole role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public List<Event> getOrganizedEvents() { return organizedEvents; }
    public List<Booking> getBookings() { return bookings; }
}