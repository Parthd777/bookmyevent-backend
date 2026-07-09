package com.bookmyevent.service;

import com.bookmyevent.model.Event;
import com.bookmyevent.storage.FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventService {
    private final Map<Long, Event> eventsById = new ConcurrentHashMap<>();
    private final FileStorage storage;
    private long nextId = 1;

    public EventService(FileStorage storage) {
        this.storage = storage;
        this.eventsById.putAll(storage.loadEvents());
        if (!eventsById.isEmpty()) {
            nextId = eventsById.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
        }
    }

    public Event createEvent(String name, String eventDate, long venueId, int totalSeats, long organizerId) {
        Event event = new Event(nextId++, name, eventDate, venueId, totalSeats, totalSeats, organizerId);
        eventsById.put(event.getId(), event);
        storage.saveAll(new ArrayList<>(storage.loadUsers().values()), new ArrayList<>(storage.loadVenues().values()), listEvents(), new ArrayList<>(storage.loadBookings().values()));
        return event;
    }

    public Event getEventById(long id) {
        return eventsById.get(id);
    }

    public List<Event> listEvents() {
        return new ArrayList<>(eventsById.values());
    }
}
