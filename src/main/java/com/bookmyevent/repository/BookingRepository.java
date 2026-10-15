package com.bookmyevent.repository;

import com.bookmyevent.model.Booking;
import com.bookmyevent.model.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b.event.id from Booking b where b.id = :id")
    Optional<Long> findEventIdByBookingId(@Param("id") long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") long id);

    @Query("select b.id from Booking b where b.status = :status and b.expiresAt <= :now order by b.id")
    List<Long> findExpiredHoldIds(@Param("status") BookingStatus status,
                                 @Param("now") LocalDateTime now, Pageable pageable);

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