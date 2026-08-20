package com.bookmyevent.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateEventRequest {
    
    @NotBlank(message = "event name must not be blank")
    private String name;
    
    private String description;
    
    @NotNull(message = "venue ID must not be null")
    private Long venueId;
    
    @NotNull(message = "organizer ID must not be null")
    private Long organizerId;
    
    @NotNull(message = "event date must not be null")
    @Future(message = "event date must be in the future")
    private LocalDateTime eventDate;
    
    @Min(value = 1, message = "total seats must be at least 1")
    private int totalSeats;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Long getVenueId() { return venueId; }
    public void setVenueId(Long venueId) { this.venueId = venueId; }
    
    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }
    
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
}