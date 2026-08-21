package com.bookmyevent.service;

import com.bookmyevent.model.Event;
import com.bookmyevent.model.User;
import com.bookmyevent.model.Venue;
import com.bookmyevent.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class EventService {
    
    private final EventRepository eventRepository;
    private final VenueService venueService;
    private final UserService userService;
    
    public EventService(EventRepository eventRepository, VenueService venueService, UserService userService) {
        this.eventRepository = eventRepository;
        this.venueService = venueService;
        this.userService = userService;
    }
    
    public Event createEvent(String name, String description, long venueId, 
                           long organizerId, LocalDateTime eventDate, int totalSeats) {
        
        Venue venue = venueService.findById(venueId);
        User organizer = userService.findById(organizerId);
        
        Event event = new Event(name, description, venue, organizer, eventDate, totalSeats);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        return eventRepository.save(event);
    }
    
    public Event findById(long id) {
        return eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found"));
    }
    
    public Page<Event> findAllEvents(@NonNull Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    public List<Event> listEvents() {
        return eventRepository.findAll();
    }

    public List<Event> listEventsWithAvailableSeats() {
        return eventRepository.findByAvailableSeatsGreaterThan(0);
    }

    public Event updateEvent(@NonNull Event event) {
        return eventRepository.save(event);
    }
}