package com.bookmyevent.repository;

import com.bookmyevent.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByEventId(Long eventId);

    @Query("select b from Booking b join fetch b.user join fetch b.event where b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") long id);

    @Query("select b from Booking b join fetch b.user join fetch b.event")
    List<Booking> findAllWithDetails();

    @Query("select b from Booking b where b.event.id = :eventId and b.status = com.bookmyevent.model.BookingStatus.CONFIRMED")
    List<Booking> findConfirmedBookingsForEvent(@Param("eventId") long eventId);

    @Query(value = "select coalesce(sum(number_of_seats), 0) from bookings " +
            "where event_id = :eventId and status = 'CONFIRMED'", nativeQuery = true)
    int sumConfirmedSeatsForEvent(@Param("eventId") long eventId);
}