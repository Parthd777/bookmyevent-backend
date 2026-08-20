package com.bookmyevent.dto.response;

import com.bookmyevent.model.Event;
import java.time.LocalDateTime;

public class EventResponse {
    private long id;
    private String name;
    private String description;
    private long venueId;
    private String venueName;
    private long organizerId;
    private String organizerName;
    private LocalDateTime eventDate;
    private int totalSeats;
    private int availableSeats;
    
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public long getVenueId() { return venueId; }
    public void setVenueId(long venueId) { this.venueId = venueId; }
    
    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    
    public long getOrganizerId() { return organizerId; }
    public void setOrganizerId(long organizerId) { this.organizerId = organizerId; }
    
    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }
    
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
    
    public static EventResponse from(Event event) {
        EventResponse response = new EventResponse();
        response.id = event.getId();
        response.name = event.getName();
        response.description = event.getDescription();
        response.venueId = event.getVenue().getId();
        response.venueName = event.getVenue().getName();
        response.organizerId = event.getCreatedBy().getId();
        response.organizerName = event.getCreatedBy().getName();
        response.eventDate = event.getEventDate();
        response.totalSeats = event.getTotalSeats();
        response.availableSeats = event.getAvailableSeats();
        return response;
    }
}