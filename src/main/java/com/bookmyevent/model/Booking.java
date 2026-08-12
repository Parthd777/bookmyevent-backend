package com.bookmyevent.model;

public class Booking extends BaseEntity {
    private long userId;
    private long eventId;
    private int seatsBooked;
    private BookingStatus status;

    public Booking() {}

    public Booking(long id, long userId, long eventId, int seatsBooked, BookingStatus status) {
        super(id);
        this.userId = userId;
        this.eventId = eventId;
        this.seatsBooked = seatsBooked;
        this.status = status;
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }
    public int getSeatsBooked() { return seatsBooked; }
    public void setSeatsBooked(int seatsBooked) { this.seatsBooked = seatsBooked; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Booking{id=" + getId() + ", userId=" + userId + ", eventId=" + eventId
                + ", seatsBooked=" + seatsBooked + ", status=" + status + "}";
    }
}
