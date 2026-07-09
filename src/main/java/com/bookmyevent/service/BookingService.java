package com.bookmyevent.service;

import com.bookmyevent.exception.BookingNotAllowedException;
import com.bookmyevent.exception.EventFullException;
import com.bookmyevent.model.Booking;
import com.bookmyevent.model.BookingStatus;
import com.bookmyevent.model.Event;
import com.bookmyevent.storage.FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BookingService {
    private final Map<Long, Booking> bookingsById = new ConcurrentHashMap<>();
    private final FileStorage storage;
    private final UserService userService;
    private final EventService eventService;
    private long nextId = 1;

    public BookingService(FileStorage storage, UserService userService, EventService eventService) {
        this.storage = storage;
        this.userService = userService;
        this.eventService = eventService;
        this.bookingsById.putAll(storage.loadBookings());
        if (!bookingsById.isEmpty()) {
            nextId = bookingsById.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
        }
    }

    public Booking createBooking(long userId, long eventId, int seatsBooked) {
        userService.getUserById(userId);
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
        event.setAvailableSeats(event.getAvailableSeats() - seatsBooked);
        storage.saveAll(new ArrayList<>(storage.loadUsers().values()), new ArrayList<>(storage.loadVenues().values()), new ArrayList<>(eventService.listEvents()), listBookings());
        return booking;
    }

    public List<Booking> listBookings() {
        return new ArrayList<>(bookingsById.values());
    }
}
