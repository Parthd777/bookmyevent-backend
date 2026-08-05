package com.bookmyevent.service;

import com.bookmyevent.model.Event;
import com.bookmyevent.storage.FileStorage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** Day 3 — Spring-managed service bean with constructor injection. */
@Service
public class EventService {

    private final Map<Long, Event> eventsById = new ConcurrentHashMap<>();
    private final FileStorage fileStorage;
    private long nextId = 1;

    public EventService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
        this.eventsById.putAll(fileStorage.loadEvents());
        if (!eventsById.isEmpty()) {
            nextId = eventsById.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
        }
    }

    public Event createEvent(String name, String eventDate, long venueId, int totalSeats, long organizerId) {
        Event event = new Event(nextId++, name, eventDate, venueId, totalSeats, totalSeats, organizerId);
        eventsById.put(event.getId(), event);
        fileStorage.saveEvents(listEvents());
        return event;
    }

    public Event getEventById(long id) {
        return eventsById.get(id);
    }

    public List<Event> listEvents() {
        return new ArrayList<>(eventsById.values());
    }

    /**
     * Persist the current in-memory event state (e.g. after seat count changes
     * triggered by BookingService).
     */
    public void persistEvents() {
        fileStorage.saveEvents(listEvents());
    }

    /** Stream-based filter — demonstrates Day 1 Streams/Lambdas, now in a Spring bean. */
    public List<Event> findEventsWithAvailableSeats() {
        return eventsById.values().stream()
                .filter(e -> e.getAvailableSeats() > 0)
                .collect(Collectors.toList());
    }
}
