package com.bookmyevent.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public class CreateBookingRequest {

    @Positive(message = "userId must be a positive number")
    private long userId;

    @Positive(message = "eventId must be a positive number")
    private long eventId;

    @Min(value = 1, message = "seatsBooked must be at least 1")
    private int seatsBooked;

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }
    public int getSeatsBooked() { return seatsBooked; }
    public void setSeatsBooked(int seatsBooked) { this.seatsBooked = seatsBooked; }
}
