package com.bookmyevent.controller;

import com.bookmyevent.dto.request.CreateBookingRequest;
import com.bookmyevent.dto.response.BookingResponse;
import com.bookmyevent.service.BookingService;
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
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * POST /bookings
     * Book seats for an event.
     * Returns 409 Conflict if no seats available, 404 if user/event not found.
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        var booking = bookingService.createBooking(
                request.getUserId(), request.getEventId(), request.getSeatsBooked());
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    /** GET /bookings */
    @GetMapping
    public ResponseEntity<List<BookingResponse>> listBookings() {
        List<BookingResponse> response = bookingService.findAll().stream()
                .map(BookingResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /** GET /bookings/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable long id) {
        return ResponseEntity.ok(BookingResponse.from(bookingService.findById(id)));
    }
}
