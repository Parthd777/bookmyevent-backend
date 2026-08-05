package com.bookmyevent.storage;

import com.bookmyevent.model.Booking;
import com.bookmyevent.model.BookingStatus;
import com.bookmyevent.model.Event;
import com.bookmyevent.model.User;
import com.bookmyevent.model.Venue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Day 3 — Repository / persistence layer.
 *
 * <p>This class is registered as a Spring bean via {@code @Bean} in
 * {@link com.bookmyevent.config.AppConfig} rather than via {@code @Repository}
 * component scanning, because its constructor throws a checked {@link IOException}
 * that needs explicit handling.
 *
 * <p>Improvement over Day 1: services now call individual {@code saveX()} methods
 * instead of reloading all other collections to rebuild the full {@code saveAll()} call.
 * Spring's singleton scope guarantees each service holds live in-memory state,
 * so there is no risk of one save overwriting another service's pending changes.
 *
 * <p>Migration path: on Day 5 this file-based repository will be replaced entirely
 * by {@code JpaRepository<Entity, Long>} interfaces backed by a real database.
 */
public class FileStorage {

    private final Path folder;

    public FileStorage(String folderName) throws IOException {
        this.folder = Paths.get(folderName);
        Files.createDirectories(folder);
    }

    // -------------------------------------------------------------------------
    // Individual save methods — each service saves only its own aggregate.
    // -------------------------------------------------------------------------

    public void saveUsers(List<User> users) {
        writeTo("users.txt", serializeUsers(users));
    }

    public void saveVenues(List<Venue> venues) {
        writeTo("venues.txt", serializeVenues(venues));
    }

    public void saveEvents(List<Event> events) {
        writeTo("events.txt", serializeEvents(events));
    }

    public void saveBookings(List<Booking> bookings) {
        writeTo("bookings.txt", serializeBookings(bookings));
    }

    // -------------------------------------------------------------------------
    // Load methods
    // -------------------------------------------------------------------------

    public Map<Long, User> loadUsers() {
        return readLines("users.txt").stream()
                .map(this::parseUser)
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> b, LinkedHashMap::new));
    }

    public Map<Long, Venue> loadVenues() {
        return readLines("venues.txt").stream()
                .map(this::parseVenue)
                .collect(Collectors.toMap(Venue::getId, v -> v, (a, b) -> b, LinkedHashMap::new));
    }

    public Map<Long, Event> loadEvents() {
        return readLines("events.txt").stream()
                .map(this::parseEvent)
                .collect(Collectors.toMap(Event::getId, e -> e, (a, b) -> b, LinkedHashMap::new));
    }

    public Map<Long, Booking> loadBookings() {
        return readLines("bookings.txt").stream()
                .map(this::parseBooking)
                .collect(Collectors.toMap(Booking::getId, b -> b, (a, bb) -> bb, LinkedHashMap::new));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void writeTo(String fileName, String content) {
        try {
            Files.writeString(folder.resolve(fileName), content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist " + fileName, e);
        }
    }

    private List<String> readLines(String fileName) {
        Path path = folder.resolve(fileName);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + fileName, e);
        }
    }

    private String serializeUsers(List<User> users) {
        return users.stream()
                .map(u -> u.getId() + "," + u.getName() + "," + u.getEmail() + "," + u.getRole())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String serializeVenues(List<Venue> venues) {
        return venues.stream()
                .map(v -> v.getId() + "," + v.getName() + "," + v.getCity() + "," + v.getCapacity())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String serializeEvents(List<Event> events) {
        return events.stream()
                .map(e -> e.getId() + "," + e.getName() + "," + e.getEventDate() + ","
                        + e.getVenueId() + "," + e.getTotalSeats() + "," + e.getAvailableSeats()
                        + "," + e.getOrganizerId())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String serializeBookings(List<Booking> bookings) {
        return bookings.stream()
                .map(b -> b.getId() + "," + b.getUserId() + "," + b.getEventId() + ","
                        + b.getSeatsBooked() + "," + b.getStatus())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private User parseUser(String line) {
        String[] p = line.split(",");
        return new User(Long.parseLong(p[0]), p[1], p[2], p[3]);
    }

    private Venue parseVenue(String line) {
        String[] p = line.split(",");
        return new Venue(Long.parseLong(p[0]), p[1], p[2], Integer.parseInt(p[3]));
    }

    private Event parseEvent(String line) {
        String[] p = line.split(",");
        return new Event(Long.parseLong(p[0]), p[1], p[2], Long.parseLong(p[3]),
                Integer.parseInt(p[4]), Integer.parseInt(p[5]), Long.parseLong(p[6]));
    }

    private Booking parseBooking(String line) {
        String[] p = line.split(",");
        return new Booking(Long.parseLong(p[0]), Long.parseLong(p[1]), Long.parseLong(p[2]),
                Integer.parseInt(p[3]), BookingStatus.valueOf(p[4]));
    }
}
