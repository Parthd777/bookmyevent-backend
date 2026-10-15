package com.bookmyevent.service;

import com.bookmyevent.exception.BookingConflictException;
import com.bookmyevent.exception.EventFullException;
import com.bookmyevent.exception.ResourceNotFoundException;
import com.bookmyevent.model.Booking;
import com.bookmyevent.model.BookingStatus;
import com.bookmyevent.model.Event;
import com.bookmyevent.model.User;
import com.bookmyevent.repository.BookingRepository;
import com.bookmyevent.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserService userService;
    private final Clock clock;
    private final Duration holdDuration;
    private final ApplicationEventPublisher publisher;
    private final CacheManager cacheManager;

    public BookingService(BookingRepository bookingRepository, EventRepository eventRepository,
                          UserService userService, Clock clock,
                          @Value("${app.booking.hold-duration:PT5M}") Duration holdDuration,
                          ApplicationEventPublisher publisher, CacheManager cacheManager) {
        if (holdDuration.isNegative() || holdDuration.isZero()) {
            throw new IllegalArgumentException("Hold duration must be positive");
        }
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.clock = clock;
        this.holdDuration = holdDuration;
        this.publisher = publisher;
        this.cacheManager = cacheManager;
    }

    public void validateBookingOwner(long userId, String email) {
        requireOwner(userService.findById(userId), email);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Booking createBooking(long userId, long eventId, int numberOfSeats) {
        return reserve(userId, eventId, numberOfSeats, BookingStatus.CONFIRMED);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Booking holdSeats(long userId, long eventId, int numberOfSeats) {
        return reserve(userId, eventId, numberOfSeats, BookingStatus.PENDING);
    }

    private Booking reserve(long userId, long eventId, int seats, BookingStatus status) {
        if (seats <= 0) {
            throw new IllegalArgumentException("Number of seats must be positive");
        }
        User user = userService.findById(userId);
        Event event = lockEvent(eventId);
        if (event.getAvailableSeats() < seats) {
            log.info("action=booking_rejected eventId={} seats={} reason=insufficient_capacity", eventId, seats);
            throw new EventFullException("Not enough seats available");
        }
        Booking booking = new Booking(user, event, seats, status);
        if (status == BookingStatus.PENDING) {
            booking.setExpiresAt(LocalDateTime.now(clock).plus(holdDuration));
        }
        event.addBooking(booking);
        event.setAvailableSeats(event.getAvailableSeats() - seats);
        Booking saved = bookingRepository.saveAndFlush(booking);
        invalidateListings();
        if (status == BookingStatus.CONFIRMED) {
            publishConfirmation(saved);
        }
        log.info("action=booking_reserved bookingId={} eventId={} seats={} status={}",
                saved.getId(), eventId, seats, status);
        return saved;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Booking confirmHold(long bookingId, String email) {
        long eventId = bookingRepository.findEventIdByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        lockEvent(eventId);
        Booking booking = lockBooking(bookingId);
        requireOwner(booking.getUser(), email);
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return booking;
        }
        if (booking.getStatus() != BookingStatus.PENDING || booking.getExpiresAt() == null
                || !booking.getExpiresAt().isAfter(LocalDateTime.now(clock))) {
            throw new BookingConflictException("Seat hold is expired or no longer pending");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setExpiresAt(null);
        bookingRepository.flush();
        publishConfirmation(booking);
        log.info("action=booking_confirmed bookingId={} eventId={}", bookingId, eventId);
        return booking;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean expireHold(long bookingId) {
        Long eventId = bookingRepository.findEventIdByBookingId(bookingId).orElse(null);
        if (eventId == null) {
            return false;
        }
        Event event = lockEvent(eventId);
        Booking booking = lockBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING || booking.getExpiresAt() == null
                || booking.getExpiresAt().isAfter(LocalDateTime.now(clock))) {
            return false;
        }
        booking.setStatus(BookingStatus.EXPIRED);
        event.setAvailableSeats(event.getAvailableSeats() + booking.getNumberOfSeats());
        invalidateListings();
        log.info("action=booking_expired bookingId={} eventId={} releasedSeats={}",
                bookingId, eventId, booking.getNumberOfSeats());
        return true;
    }

    private Event lockEvent(long eventId) {
        return eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    private Booking lockBooking(long bookingId) {
        return bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private void requireOwner(User user, String email) {
        if (!user.getEmail().equals(email)) {
            throw new AccessDeniedException("Booking belongs to another customer");
        }
    }

    private void invalidateListings() {
        Objects.requireNonNull(cacheManager.getCache("eventListings")).clear();
    }

    private void publishConfirmation(Booking booking) {
        publisher.publishEvent(new BookingConfirmedEvent(booking.getId(), booking.getUser().getId(),
                booking.getEvent().getId(), booking.getNumberOfSeats()));
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(long id) {
        return bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return bookingRepository.findAllWithDetails();
    }

    public Booking findById(long id) {
        return getBookingById(id);
    }
}
