package com.bookmyevent.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class CreateEventRequest {

    @NotBlank(message = "event name must not be blank")
    private String name;

    @NotBlank(message = "event date must not be blank")
    private String eventDate;

    @Positive(message = "venueId must be a positive number")
    private long venueId;

    @Min(value = 1, message = "totalSeats must be at least 1")
    private int totalSeats;

    @Positive(message = "organizerId must be a positive number")
    private long organizerId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public long getVenueId() { return venueId; }
    public void setVenueId(long venueId) { this.venueId = venueId; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public long getOrganizerId() { return organizerId; }
    public void setOrganizerId(long organizerId) { this.organizerId = organizerId; }
}
