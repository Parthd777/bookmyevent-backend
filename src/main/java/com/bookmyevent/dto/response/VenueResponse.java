package com.bookmyevent.dto.response;

import com.bookmyevent.model.Venue;

public class VenueResponse {
    private long id;
    private String name;
    private String city;
    private int capacity;

    public static VenueResponse from(Venue venue) {
        VenueResponse r = new VenueResponse();
        r.id = venue.getId();
        r.name = venue.getName();
        r.city = venue.getCity();
        r.capacity = venue.getCapacity();
        return r;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public int getCapacity() { return capacity; }
}
