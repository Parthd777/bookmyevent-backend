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

public class FileStorage {
    private final Path folder;

    public FileStorage(String folderName) throws IOException {
        this.folder = Paths.get(folderName);
        Files.createDirectories(folder);
    }

    public void saveAll(List<User> users, List<Venue> venues, List<Event> events, List<Booking> bookings) {
        try {
            Files.writeString(folder.resolve("users.txt"), serializeUsers(users));
            Files.writeString(folder.resolve("venues.txt"), serializeVenues(venues));
            Files.writeString(folder.resolve("events.txt"), serializeEvents(events));
            Files.writeString(folder.resolve("bookings.txt"), serializeBookings(bookings));
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist data", e);
        }
    }

    public Map<Long, User> loadUsers() {
        return readLines(folder.resolve("users.txt")).stream()
                .map(this::parseUser)
                .collect(Collectors.toMap(User::getId, user -> user, (a, b) -> b, LinkedHashMap::new));
    }

    public Map<Long, Venue> loadVenues() {
        return readLines(folder.resolve("venues.txt")).stream()
                .map(this::parseVenue)
                .collect(Collectors.toMap(Venue::getId, venue -> venue, (a, b) -> b, LinkedHashMap::new));
    }

    public Map<Long, Event> loadEvents() {
        return readLines(folder.resolve("events.txt")).stream()
                .map(this::parseEvent)
                .collect(Collectors.toMap(Event::getId, event -> event, (a, b) -> b, LinkedHashMap::new));
    }

    public Map<Long, Booking> loadBookings() {
        return readLines(folder.resolve("bookings.txt")).stream()
                .map(this::parseBooking)
                .collect(Collectors.toMap(Booking::getId, booking -> booking, (a, b) -> b, LinkedHashMap::new));
    }

    private List<String> readLines(Path path) {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file " + path, e);
        }
    }

    private String serializeUsers(List<User> users) {
        return users.stream().map(u -> u.getId() + "," + u.getName() + "," + u.getEmail() + "," + u.getRole()).collect(Collectors.joining(System.lineSeparator()));
    }

    private String serializeVenues(List<Venue> venues) {
        return venues.stream().map(v -> v.getId() + "," + v.getName() + "," + v.getCity() + "," + v.getCapacity()).collect(Collectors.joining(System.lineSeparator()));
    }

    private String serializeEvents(List<Event> events) {
        return events.stream().map(e -> e.getId() + "," + e.getName() + "," + e.getEventDate() + "," + e.getVenueId() + "," + e.getTotalSeats() + "," + e.getAvailableSeats() + "," + e.getOrganizerId()).collect(Collectors.joining(System.lineSeparator()));
    }

    private String serializeBookings(List<Booking> bookings) {
        return bookings.stream().map(b -> b.getId() + "," + b.getUserId() + "," + b.getEventId() + "," + b.getSeatsBooked() + "," + b.getStatus()).collect(Collectors.joining(System.lineSeparator()));
    }

    private User parseUser(String line) {
        String[] parts = line.split(",");
        return new User(Long.parseLong(parts[0]), parts[1], parts[2], parts[3]);
    }

    private Venue parseVenue(String line) {
        String[] parts = line.split(",");
        return new Venue(Long.parseLong(parts[0]), parts[1], parts[2], Integer.parseInt(parts[3]));
    }

    private Event parseEvent(String line) {
        String[] parts = line.split(",");
        return new Event(Long.parseLong(parts[0]), parts[1], parts[2], Long.parseLong(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), Long.parseLong(parts[6]));
    }

    private Booking parseBooking(String line) {
        String[] parts = line.split(",");
        return new Booking(Long.parseLong(parts[0]), Long.parseLong(parts[1]), Long.parseLong(parts[2]), Integer.parseInt(parts[3]), BookingStatus.valueOf(parts[4]));
    }
}
