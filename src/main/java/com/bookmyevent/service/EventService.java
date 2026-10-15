package com.bookmyevent.service;

import com.bookmyevent.model.Event;
import com.bookmyevent.model.User;
import com.bookmyevent.model.Venue;
import com.bookmyevent.dto.response.EventResponse;
import com.bookmyevent.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
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
    
    @CacheEvict(cacheNames = "eventListings", allEntries = true)
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
        return eventRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new RuntimeException("Event not found"));
    }
    
    public Page<Event> findAllEvents(@NonNull Pageable pageable) {
        return eventRepository.findAllWithDetails(pageable);
    }

    public List<Event> listEvents() {
        return eventRepository.findAllWithDetailsUnpaged();
    }

    @Cacheable(cacheNames = "eventListings", key = "'all-events'")
    public List<EventResponse> listEventResponses() {
        return listEvents().stream().map(EventResponse::from).toList();
    }

    public List<Event> listEventsWithAvailableSeats() {
        return eventRepository.findByAvailableSeatsGreaterThan(0);
    }

    public Event updateEvent(@NonNull Event event) {
        return eventRepository.save(event);
    }
}