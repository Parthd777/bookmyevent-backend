package com.bookmyevent.dto.response;

import com.bookmyevent.model.Booking;
import com.bookmyevent.model.BookingStatus;

public class BookingResponse {
    private long id;
    private long userId;
    private long eventId;
    private int seatsBooked;
    private BookingStatus status;

    public static BookingResponse from(Booking booking) {
        BookingResponse r = new BookingResponse();
        r.id = booking.getId();
        r.userId = booking.getUserId();
        r.eventId = booking.getEventId();
        r.seatsBooked = booking.getSeatsBooked();
        r.status = booking.getStatus();
        return r;
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public long getEventId() { return eventId; }
    public int getSeatsBooked() { return seatsBooked; }
    public BookingStatus getStatus() { return status; }
}
