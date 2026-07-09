package com.bookmyevent.service;

import com.bookmyevent.model.Venue;
import com.bookmyevent.storage.FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VenueService {
    private final Map<Long, Venue> venuesById = new ConcurrentHashMap<>();
    private final FileStorage storage;
    private long nextId = 1;

    public VenueService(FileStorage storage) {
        this.storage = storage;
        this.venuesById.putAll(storage.loadVenues());
        if (!venuesById.isEmpty()) {
            nextId = venuesById.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
        }
    }

    public Venue createVenue(String name, String city, int capacity) {
        Venue venue = new Venue(nextId++, name, city, capacity);
        venuesById.put(venue.getId(), venue);
        storage.saveAll(new ArrayList<>(storage.loadUsers().values()), listVenues(), new ArrayList<>(storage.loadEvents().values()), new ArrayList<>(storage.loadBookings().values()));
        return venue;
    }

    public Venue getVenueById(long id) {
        return venuesById.get(id);
    }

    public List<Venue> listVenues() {
        return new ArrayList<>(venuesById.values());
    }
}
