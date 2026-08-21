package com.bookmyevent.service;

import com.bookmyevent.model.Venue;
import com.bookmyevent.repository.VenueRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class VenueService {
    
    private final VenueRepository venueRepository;
    
    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }
    
    public Venue createVenue(String name, String city, int capacity) {
        Venue venue = new Venue(name, city, capacity);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        venue.setCreatedAt(now);
        venue.setUpdatedAt(now);
        return venueRepository.save(venue);
    }

    public List<Venue> listVenues() {
        return venueRepository.findAll();
    }
    
    public Venue findById(long id) {
        return venueRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Venue not found"));
    }
}