package com.bookmyevent.controller;

import com.bookmyevent.dto.request.CreateVenueRequest;
import com.bookmyevent.dto.response.VenueResponse;
import com.bookmyevent.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody CreateVenueRequest request) {
        var venue = venueService.createVenue(request.getName(), request.getCity(), request.getCapacity());
        return ResponseEntity.status(HttpStatus.CREATED).body(VenueResponse.from(venue));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> getVenue(@PathVariable long id) {
        return ResponseEntity.ok(VenueResponse.from(venueService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<List<VenueResponse>> listVenues() {
        List<VenueResponse> response = venueService.listVenues().stream()
                .map(VenueResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
