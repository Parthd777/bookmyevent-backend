package com.bookmyevent.repository;

import com.bookmyevent.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByEventId(Long eventId);

    @Query("select b from Booking b where b.event.id = :eventId and b.status = com.bookmyevent.model.BookingStatus.CONFIRMED")
    List<Booking> findConfirmedBookingsForEvent(@Param("eventId") long eventId);

    @Query(value = "select coalesce(sum(number_of_seats), 0) from bookings " +
            "where event_id = :eventId and status = 'CONFIRMED'", nativeQuery = true)
    int sumConfirmedSeatsForEvent(@Param("eventId") long eventId);
}