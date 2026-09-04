package com.bookmyevent.repository;

import com.bookmyevent.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByVenueId(Long venueId);
    List<Event> findByCreatedById(Long organizerId);
    List<Event> findByAvailableSeatsGreaterThan(int availableSeats);

    @Query("select e from Event e join fetch e.venue join fetch e.createdBy " +
            "where e.availableSeats > 0 order by e.eventDate asc")
    List<Event> findAvailableEventsWithDetails();

    @Query(value = "select e from Event e join fetch e.venue join fetch e.createdBy",
            countQuery = "select count(e) from Event e")
    org.springframework.data.domain.Page<Event> findAllWithDetails(org.springframework.data.domain.Pageable pageable);

    @Query("select e from Event e join fetch e.venue join fetch e.createdBy")
    List<Event> findAllWithDetailsUnpaged();

    @Query("select e from Event e join fetch e.venue join fetch e.createdBy where e.id = :id")
    Optional<Event> findByIdWithDetails(@Param("id") long id);

    @Query("select e from Event e where e.venue.city = :city and e.eventDate >= :from " +
            "order by e.eventDate asc")
    List<Event> findUpcomingEventsInCity(@Param("city") String city, @Param("from") LocalDateTime from);

    @Query(value = "select e.* from events e " +
            "left join bookings b on b.event_id = e.id and b.status = 'CONFIRMED' " +
            "group by e.id order by count(b.id) desc limit :limit", nativeQuery = true)
    List<Event> findMostBookedEvents(@Param("limit") int limit);
}