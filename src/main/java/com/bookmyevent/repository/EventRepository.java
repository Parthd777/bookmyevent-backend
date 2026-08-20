package com.bookmyevent.repository;

import com.bookmyevent.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByVenueId(Long venueId);
    List<Event> findByCreatedById(Long organizerId);
    List<Event> findByAvailableSeatsGreaterThan(int availableSeats);
}