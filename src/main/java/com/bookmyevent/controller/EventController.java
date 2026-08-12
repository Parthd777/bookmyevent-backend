package com.bookmyevent.controller;

import com.bookmyevent.dto.request.CreateEventRequest;
import com.bookmyevent.dto.response.EventResponse;
import com.bookmyevent.service.EventService;
import com.bookmyevent.service.UserService;
import com.bookmyevent.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final VenueService venueService;
    private final UserService userService;

    public EventController(EventService eventService, VenueService venueService, UserService userService) {
        this.eventService = eventService;
        this.venueService = venueService;
        this.userService = userService;
    }

    /**
     * POST /events
     * Validates that the referenced venue and organizer exist before creating the event.
     * Returns 400 if either is missing (GlobalExceptionHandler converts the exception).
     */
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        venueService.getVenueById(request.getVenueId());       // validate venue exists
        userService.getUserById(request.getOrganizerId());      // validate organizer exists
        var event = eventService.createEvent(
                request.getName(), request.getEventDate(),
                request.getVenueId(), request.getTotalSeats(), request.getOrganizerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(event));
    }

    /** GET /events */
    @GetMapping
    public ResponseEntity<List<EventResponse>> listEvents() {
        List<EventResponse> response = eventService.listEvents().stream()
                .map(EventResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /** GET /events/available — only events with remaining seats */
    @GetMapping("/available")
    public ResponseEntity<List<EventResponse>> listAvailableEvents() {
        List<EventResponse> response = eventService.listEventsWithAvailableSeats().stream()
                .map(EventResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /** GET /events/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable long id) {
        return ResponseEntity.ok(EventResponse.from(eventService.getEventById(id)));
    }
}
