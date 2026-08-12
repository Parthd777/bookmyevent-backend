package com.bookmyevent.dto.response;

import com.bookmyevent.model.Event;

public class EventResponse {
    private long id;
    private String name;
    private String eventDate;
    private long venueId;
    private int totalSeats;
    private int availableSeats;
    private long organizerId;

    public static EventResponse from(Event event) {
        EventResponse r = new EventResponse();
        r.id = event.getId();
        r.name = event.getName();
        r.eventDate = event.getEventDate();
        r.venueId = event.getVenueId();
        r.totalSeats = event.getTotalSeats();
        r.availableSeats = event.getAvailableSeats();
        r.organizerId = event.getOrganizerId();
        return r;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getEventDate() { return eventDate; }
    public long getVenueId() { return venueId; }
    public int getTotalSeats() { return totalSeats; }
    public int getAvailableSeats() { return availableSeats; }
    public long getOrganizerId() { return organizerId; }
}
