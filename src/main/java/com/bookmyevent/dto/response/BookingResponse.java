package com.bookmyevent.dto.response;

import com.bookmyevent.model.Booking;
import com.bookmyevent.model.BookingStatus;
import java.time.LocalDateTime;

public class BookingResponse {
    private long id;
    private long userId;
    private long eventId;
    private int seatsBooked;
    private BookingStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BookingResponse from(Booking booking) {
        BookingResponse r = new BookingResponse();
        r.id = booking.getId();
        r.userId = booking.getUser().getId();
        r.eventId = booking.getEvent().getId();
        r.seatsBooked = booking.getNumberOfSeats();
        r.status = booking.getStatus();
        r.expiresAt = booking.getExpiresAt();
        r.createdAt = booking.getCreatedAt();
        r.updatedAt = booking.getUpdatedAt();
        return r;
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public long getEventId() { return eventId; }
    public int getSeatsBooked() { return seatsBooked; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
