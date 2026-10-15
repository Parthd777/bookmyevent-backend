package com.bookmyevent.service;

import com.bookmyevent.model.BookingStatus;
import com.bookmyevent.repository.BookingRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class SeatHoldCleanupJob {
    private static final Logger log = LoggerFactory.getLogger(SeatHoldCleanupJob.class);
    private final BookingRepository repository;
    private final BookingService service;
    private final Clock clock;
    private final MeterRegistry meterRegistry;

    public SeatHoldCleanupJob(BookingRepository repository, BookingService service,
                              Clock clock, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.service = service;
        this.clock = clock;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedDelayString = "${app.booking.cleanup-delay-ms:30000}",
            initialDelayString = "${app.booking.cleanup-delay-ms:30000}")
    public void cleanUpExpiredHolds() {
        for (Long bookingId : repository.findExpiredHoldIds(BookingStatus.PENDING,
                LocalDateTime.now(clock), PageRequest.of(0, 100))) {
            try {
                if (service.expireHold(bookingId)) {
                    meterRegistry.counter("booking.holds.expired").increment();
                }
            } catch (RuntimeException exception) {
                log.error("action=hold_cleanup_failed bookingId={}", bookingId, exception);
                meterRegistry.counter("booking.holds.cleanup.failures").increment();
            }
        }
    }
}