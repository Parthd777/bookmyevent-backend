package com.bookmyevent.controller;

import com.bookmyevent.dto.request.CreateEventRequest;
import com.bookmyevent.dto.response.EventResponse;
import com.bookmyevent.service.EventService;
import com.bookmyevent.service.UserService;
import com.bookmyevent.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

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

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        venueService.findById(request.getVenueId());       // validate venue exists
        userService.findById(request.getOrganizerId());      // validate organizer exists
        var event = eventService.createEvent(
            request.getName(), request.getDescription(),
            request.getVenueId(), request.getOrganizerId(),
            request.getEventDate(), request.getTotalSeats());
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(event));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> listEvents() {
        List<EventResponse> response = eventService.listEvents().stream()
                .map(EventResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<List<EventResponse>> listAvailableEvents() {
        List<EventResponse> response = eventService.listEventsWithAvailableSeats().stream()
                .map(EventResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable long id) {
        return ResponseEntity.ok(EventResponse.from(eventService.findById(id)));
    }

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("eventDate", "name", "availableSeats", "totalSeats");

    @GetMapping("/page")
    public ResponseEntity<Page<EventResponse>> listEventsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "eventDate") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        String safeSortBy = SORTABLE_FIELDS.contains(sortBy) ? sortBy : "eventDate";
        Sort.Direction safeDirection = "DESC".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 50),
                Sort.by(safeDirection, safeSortBy));

        Page<EventResponse> response = eventService.findAllEvents(pageable).map(EventResponse::from);
        return ResponseEntity.ok(response);
    }
}
