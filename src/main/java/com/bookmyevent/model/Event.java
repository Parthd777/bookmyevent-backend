package com.bookmyevent.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event extends BaseEntity {
    private String name;
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;
    
    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private User createdBy;
    
    private LocalDateTime eventDate;
    private int totalSeats;
    private int availableSeats;

    public Event() {}
    public Event(String name, String description, Venue venue, User createdBy, 
                 LocalDateTime eventDate, int totalSeats) {
        this.name = name;
        this.description = description;
        this.venue = venue;
        this.createdBy = createdBy;
        this.eventDate = eventDate;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }
    
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
}