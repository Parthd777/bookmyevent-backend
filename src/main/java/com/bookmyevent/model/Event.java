package com.bookmyevent.model;

public class Event extends BaseEntity {
    private String name;
    private String eventDate;
    private long venueId;
    private int totalSeats;
    private int availableSeats;
    private long organizerId;

    public Event() {}

    public Event(long id, String name, String eventDate, long venueId,
                 int totalSeats, int availableSeats, long organizerId) {
        super(id);
        this.name = name;
        this.eventDate = eventDate;
        this.venueId = venueId;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.organizerId = organizerId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public long getVenueId() { return venueId; }
    public void setVenueId(long venueId) { this.venueId = venueId; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
    public long getOrganizerId() { return organizerId; }
    public void setOrganizerId(long organizerId) { this.organizerId = organizerId; }

    @Override
    public String toString() {
        return "Event{id=" + getId() + ", name='" + name + "', date='" + eventDate
                + "', venueId=" + venueId + ", availableSeats=" + availableSeats + "}";
    }
}
