package com.bookmyevent.service;

public record BookingConfirmedEvent(long bookingId, long userId, long eventId, int seats) {
}