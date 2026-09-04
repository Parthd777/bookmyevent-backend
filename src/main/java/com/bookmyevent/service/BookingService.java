package com.bookmyevent.service;

import com.bookmyevent.exception.EventFullException;
import com.bookmyevent.model.Booking;
import com.bookmyevent.model.BookingStatus;
import com.bookmyevent.model.Event;
import com.bookmyevent.model.User;
import com.bookmyevent.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    
    @Transactional
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
        event.addBooking(booking); // keeps event.getBookings() in sync with the owning FK side
        
        event.setAvailableSeats(event.getAvailableSeats() - numberOfSeats);
        event.setUpdatedAt(now);
        eventService.updateEvent(event);
        
        return bookingRepository.save(booking);
    }
    
    public Booking getBookingById(long id) {
        return bookingRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public List<Booking> findAll() {
        return bookingRepository.findAllWithDetails();
    }

    public Booking findById(long id) {
        return getBookingById(id);
    }
}