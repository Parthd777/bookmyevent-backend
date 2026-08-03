package com.bookmyevent.service;

import com.bookmyevent.model.Venue;
import com.bookmyevent.storage.FileStorage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Day 3 — Spring-managed service bean with constructor injection. */
@Service
public class VenueService {

    private final Map<Long, Venue> venuesById = new ConcurrentHashMap<>();
    private final FileStorage fileStorage;
    private long nextId = 1;

    public VenueService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
        this.venuesById.putAll(fileStorage.loadVenues());
        if (!venuesById.isEmpty()) {
            nextId = venuesById.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
        }
    }

    public Venue createVenue(String name, String city, int capacity) {
        Venue venue = new Venue(nextId++, name, city, capacity);
        venuesById.put(venue.getId(), venue);
        fileStorage.saveVenues(listVenues());
        return venue;
    }

    public Venue getVenueById(long id) {
        return venuesById.get(id);
    }

    public List<Venue> listVenues() {
        return new ArrayList<>(venuesById.values());
    }
}
