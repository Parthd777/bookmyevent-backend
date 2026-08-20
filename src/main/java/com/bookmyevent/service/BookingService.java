package com.bookmyevent.service;

import com.bookmyevent.exception.EventFullException;
import com.bookmyevent.model.Booking;
import com.bookmyevent.model.BookingStatus;
import com.bookmyevent.model.Event;
import com.bookmyevent.model.User;
import com.bookmyevent.repository.BookingRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final EventService eventService;
    private final UserService userService;
    
    public BookingService(BookingRepository bookingRepository, EventService eventService, UserService userService) {
        this.bookingRepository = bookingRepository;
        this.eventService = eventService;
        this.userService = userService;
    }
    
    public Booking createBooking(long userId, long eventId, int numberOfSeats) {
        User user = userService.findById(userId);
        Event event = eventService.findById(eventId);
        
        if (event.getAvailableSeats() < numberOfSeats) {
            throw new EventFullException("Not enough seats available");
        }
        
        Booking booking = new Booking(user, event, numberOfSeats, BookingStatus.CONFIRMED);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);
        
        event.setAvailableSeats(event.getAvailableSeats() - numberOfSeats);
        event.setUpdatedAt(now);
        eventService.updateEvent(event);
        
        return bookingRepository.save(booking);
    }
    
    public Booking getBookingById(long id) {
        return bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Booking findById(long id) {
        return getBookingById(id);
    }
}