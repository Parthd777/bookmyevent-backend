package com.bookmyevent.service;

import com.bookmyevent.exception.BookingNotAllowedException;
import com.bookmyevent.exception.EventFullException;
import com.bookmyevent.model.Booking;
import com.bookmyevent.model.BookingStatus;
import com.bookmyevent.model.Event;
import com.bookmyevent.storage.FileStorage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookingService {

    private final Map<Long, Booking> bookingsById = new ConcurrentHashMap<>();
    private final FileStorage fileStorage;
    private final UserService userService;
    private final EventService eventService;
    private long nextId = 1;

    public BookingService(FileStorage fileStorage, UserService userService, EventService eventService) {
        this.fileStorage = fileStorage;
        this.userService = userService;
        this.eventService = eventService;
        this.bookingsById.putAll(fileStorage.loadBookings());
        if (!bookingsById.isEmpty()) {
            nextId = bookingsById.keySet().stream().max(Comparator.naturalOrder()).orElse(0L) + 1;
        }
    }

    public Booking createBooking(long userId, long eventId, int seatsBooked) {
        userService.getUserById(userId); // throws UserNotFoundException if absent

        Event event = eventService.getEventById(eventId); // throws BookingNotAllowedException if absent
        if (seatsBooked <= 0) {
            throw new BookingNotAllowedException("Seats must be positive.");
        }
        if (event.getAvailableSeats() < seatsBooked) {
            throw new EventFullException("Not enough seats available for event: " + event.getName());
        }

        Booking booking = new Booking(nextId++, userId, eventId, seatsBooked, BookingStatus.CONFIRMED);
        bookingsById.put(booking.getId(), booking);

        event.setAvailableSeats(event.getAvailableSeats() - seatsBooked);
        fileStorage.saveBookings(listBookings());
        eventService.persistEvents();

        return booking;
    }

    public Booking getBookingById(long id) {
        return Optional.ofNullable(bookingsById.get(id))
                .orElseThrow(() -> new BookingNotAllowedException("Booking not found with id " + id));
    }

    public List<Booking> listBookings() {
        return new ArrayList<>(bookingsById.values());
    }
}
