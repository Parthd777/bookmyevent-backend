package com.bookmyevent.service;

import com.bookmyevent.exception.BookingNotAllowedException;
import com.bookmyevent.model.Event;
import com.bookmyevent.storage.FileStorage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EventService {

    private final Map<Long, Event> eventsById = new ConcurrentHashMap<>();
    private final FileStorage fileStorage;
    private long nextId = 1;

    public EventService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
        this.eventsById.putAll(fileStorage.loadEvents());
        if (!eventsById.isEmpty()) {
            nextId = eventsById.keySet().stream().max(Comparator.naturalOrder()).orElse(0L) + 1;
        }
    }

    public Event createEvent(String name, String eventDate, long venueId, int totalSeats, long organizerId) {
        Event event = new Event(nextId++, name, eventDate, venueId, totalSeats, totalSeats, organizerId);
        eventsById.put(event.getId(), event);
        fileStorage.saveEvents(listEvents());
        return event;
    }

    public Event getEventById(long id) {
        Event event = eventsById.get(id);
        if (event == null) throw new BookingNotAllowedException("Event not found with id " + id);
        return event;
    }

    public List<Event> listEvents() {
        return new ArrayList<>(eventsById.values());
    }

    public List<Event> listEventsWithAvailableSeats() {
        return eventsById.values().stream()
                .filter(e -> e.getAvailableSeats() > 0)
                .toList();
    }

    public void persistEvents() {
        fileStorage.saveEvents(listEvents());
    }
}
