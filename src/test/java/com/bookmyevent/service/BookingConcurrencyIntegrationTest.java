package com.bookmyevent.service;

import com.bookmyevent.exception.EventFullException;
import com.bookmyevent.model.Booking;
import com.bookmyevent.model.BookingStatus;
import com.bookmyevent.model.Event;
import com.bookmyevent.model.User;
import com.bookmyevent.model.UserRole;
import com.bookmyevent.model.Venue;
import com.bookmyevent.repository.BookingRepository;
import com.bookmyevent.repository.EventRepository;
import com.bookmyevent.repository.UserRepository;
import com.bookmyevent.repository.VenueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:bookmyevent-day7;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.booking.cleanup-delay-ms=3600000"
})
class BookingConcurrencyIntegrationTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void onlyOneConcurrentRequestCanBookTheLastSeat() throws Exception {
        User user = createUser("race@example.test");
        Event event = createEvent(user, 1);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            Future<Booking> first = workers.submit(() -> bookAfterBarrier(ready, start, user, event));
            Future<Booking> second = workers.submit(() -> bookAfterBarrier(ready, start, user, event));
            ready.await();
            start.countDown();

            int successes = 0;
            int fullResponses = 0;
            for (Future<Booking> attempt : new Future[]{first, second}) {
                try {
                    assertEquals(BookingStatus.CONFIRMED, attempt.get().getStatus());
                    successes++;
                } catch (ExecutionException exception) {
                    assertInstanceOf(EventFullException.class, exception.getCause());
                    fullResponses++;
                }
            }
            assertEquals(1, successes);
            assertEquals(1, fullResponses);
            assertEquals(0, eventRepository.findById(event.getId()).orElseThrow().getAvailableSeats());
            assertEquals(1, bookingRepository.findByEventId(event.getId()).size());
        } finally {
            workers.shutdownNow();
        }
    }

    @Test
    void expiredHoldReleasesCapacityExactlyOnce() {
        User user = createUser("expiry@example.test");
        Event event = createEvent(user, 2);
        Booking hold = bookingService.holdSeats(user.getId(), event.getId(), 1);
        hold.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1));
        bookingRepository.saveAndFlush(hold);

        assertTrue(bookingService.expireHold(hold.getId()));
        assertFalse(bookingService.expireHold(hold.getId()));
        assertEquals(BookingStatus.EXPIRED, bookingRepository.findById(hold.getId()).orElseThrow().getStatus());
        assertEquals(2, eventRepository.findById(event.getId()).orElseThrow().getAvailableSeats());
    }

    @Test
    void onlyHoldOwnerCanConfirmAndConfirmationIsIdempotent() {
        User user = createUser("confirm@example.test");
        Event event = createEvent(user, 2);
        Booking hold = bookingService.holdSeats(user.getId(), event.getId(), 1);

        org.junit.jupiter.api.Assertions.assertThrows(AccessDeniedException.class,
                () -> bookingService.confirmHold(hold.getId(), "other@example.test"));
        Booking confirmed = bookingService.confirmHold(hold.getId(), user.getEmail());
        Booking repeated = bookingService.confirmHold(hold.getId(), user.getEmail());

        assertEquals(BookingStatus.CONFIRMED, confirmed.getStatus());
        assertNull(confirmed.getExpiresAt());
        assertEquals(confirmed.getId(), repeated.getId());
        assertEquals(1, eventRepository.findById(event.getId()).orElseThrow().getAvailableSeats());
    }

    private Booking bookAfterBarrier(CountDownLatch ready, CountDownLatch start, User user, Event event)
            throws InterruptedException {
        ready.countDown();
        start.await();
        return bookingService.createBooking(user.getId(), event.getId(), 1);
    }

    private User createUser(String email) {
        return userRepository.saveAndFlush(new User("Day Seven", email, UserRole.CUSTOMER));
    }

    private Event createEvent(User organizer, int seats) {
        Venue venue = venueRepository.saveAndFlush(new Venue("Concurrency Hall", "Test City", 100));
        return eventRepository.saveAndFlush(new Event("Last Seat Test", "", venue, organizer,
                LocalDateTime.now().plusDays(1), seats));
    }
}