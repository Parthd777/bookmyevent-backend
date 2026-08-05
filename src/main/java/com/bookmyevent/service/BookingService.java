package com.bookmyevent.service;

import com.bookmyevent.exception.BookingNotAllowedException;
import com.bookmyevent.exception.EventFullException;
import com.bookmyevent.model.Booking;
import com.bookmyevent.model.BookingStatus;
import com.bookmyevent.model.Event;
import com.bookmyevent.storage.FileStorage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Day 3 — Spring-managed service bean with constructor injection.
 *
 * <p>BookingService depends on both {@link UserService} and {@link EventService}.
 * On Day 1 these were passed manually:
 * <pre>
 *   BookingService bookingService = new BookingService(storage, userService, eventService);
 * </pre>
 * On Day 3 Spring resolves this dependency graph automatically at startup.
 */
@Service
public class BookingService {

    private final Map<Long, Booking> bookingsById = new ConcurrentHashMap<>();
    private final FileStorage fileStorage;
    private final UserService userService;
    private final EventService eventService;
    private long nextId = 1;

    // Spring injects FileStorage, UserService, EventService — all singletons.
    public BookingService(FileStorage fileStorage, UserService userService, EventService eventService) {
        this.fileStorage = fileStorage;
        this.userService = userService;
        this.eventService = eventService;
        this.bookingsById.putAll(fileStorage.loadBookings());
        if (!bookingsById.isEmpty()) {
            nextId = bookingsById.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
        }
    }

    public Booking createBooking(long userId, long eventId, int seatsBooked) {
        userService.getUserById(userId); // throws UserNotFoundException if absent

        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new BookingNotAllowedException("Event not found with id " + eventId);
        }
        if (seatsBooked <= 0) {
            throw new BookingNotAllowedException("Seats must be positive.");
        }
        if (event.getAvailableSeats() < seatsBooked) {
            throw new EventFullException("Not enough seats available for event " + event.getName());
        }

        Booking booking = new Booking(nextId++, userId, eventId, seatsBooked, BookingStatus.CONFIRMED);
        bookingsById.put(booking.getId(), booking);

        // Update available seats on the shared Event object (same singleton in EventService)
        event.setAvailableSeats(event.getAvailableSeats() - seatsBooked);

        // Persist: only save the collections that actually changed
        fileStorage.saveBookings(listBookings());
        eventService.persistEvents(); // persist updated available_seats

        return booking;
    }

    public List<Booking> listBookings() {
        return new ArrayList<>(bookingsById.values());
    }
}
