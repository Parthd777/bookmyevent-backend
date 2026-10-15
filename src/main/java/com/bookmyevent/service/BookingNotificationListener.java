package com.bookmyevent.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BookingNotificationListener {
    private static final Logger log = LoggerFactory.getLogger(BookingNotificationListener.class);
    private final MeterRegistry meterRegistry;

    public BookingNotificationListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifyCustomer(BookingConfirmedEvent event) {
        log.info("action=booking_email_simulated bookingId={} userId={} eventId={} seats={} thread={}",
                event.bookingId(), event.userId(), event.eventId(), event.seats(), Thread.currentThread().getName());
        meterRegistry.counter("booking.notifications.simulated").increment();
    }
}