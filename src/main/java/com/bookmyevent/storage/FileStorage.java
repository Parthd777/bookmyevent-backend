package com.bookmyevent.storage;

import com.bookmyevent.model.Booking;
import com.bookmyevent.model.BookingStatus;
import com.bookmyevent.model.Event;
import com.bookmyevent.model.User;
import com.bookmyevent.model.Venue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Day 4 — Repository layer with AES-256-GCM encrypted file storage.
 *
 * <h3>Why @Repository (not @Bean in AppConfig like Day 3)?</h3>
 * <p>In Spring Boot we handle the checked {@link IOException} by rethrowing as
 * {@link UncheckedIOException} in the constructor body. This makes the constructor
 * compatible with {@code @Repository} + {@code @Value} — Spring Boot creates the bean
 * during component scan without needing an explicit {@code @Bean} factory method.
 *
 * <h3>Encryption: AES-256-GCM</h3>
 * <ul>
 *   <li><b>Algorithm</b>: AES/GCM/NoPadding — authenticated encryption (AEAD)</li>
 *   <li><b>Key size</b>: 256 bits (32 bytes), read from {@code application.properties}</li>
 *   <li><b>IV</b>: 12 bytes (96-bit), randomly generated per file write</li>
 *   <li><b>Auth tag</b>: 128 bits — GCM appends this after the ciphertext; decryption fails
 *       if the file was tampered with</li>
 *   <li><b>Storage format</b>: {@code Base64( IV[12] || ciphertext || authTag[16] )}</li>
 * </ul>
 *
 * <p>GCM is preferred over CBC because:
 * <ul>
 *   <li>No padding oracle vulnerability (no padding at all)</li>
 *   <li>Integrity check built-in — any bit-flip in the file causes an {@link javax.crypto.AEADBadTagException}</li>
 *   <li>Parallelisable encryption (faster)</li>
 * </ul>
 *
 * <h3>What changes on Day 5?</h3>
 * <p>This entire class is replaced by JPA {@code @Entity} classes and
 * {@code JpaRepository<Entity, Long>} interfaces. The encryption concern
 * moves to the database layer (column-level encryption or database TDE).
 */
@Repository
public class FileStorage {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_BITS  = 128;

    private final Path folder;
    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Spring Boot injects values from {@code application.properties}.
     *
     * @param folderName    storage directory path (default: {@code data})
     * @param base64Key     Base64-encoded 32-byte AES-256 key
     */
    public FileStorage(
            @Value("${app.storage.folder:data}") String folderName,
            @Value("${app.storage.encryption-key}") String base64Key) {
        try {
            this.folder = Paths.get(folderName);
            Files.createDirectories(folder);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create storage folder: " + folderName, e);
        }
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Save methods — each service saves only its own aggregate
    // ─────────────────────────────────────────────────────────────────────────

    public void saveUsers(List<User> users) {
        writeTo("users.dat", serializeUsers(users));
    }

    public void saveVenues(List<Venue> venues) {
        writeTo("venues.dat", serializeVenues(venues));
    }

    public void saveEvents(List<Event> events) {
        writeTo("events.dat", serializeEvents(events));
    }

    public void saveBookings(List<Booking> bookings) {
        writeTo("bookings.dat", serializeBookings(bookings));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Load methods
    // ─────────────────────────────────────────────────────────────────────────

    public Map<Long, User> loadUsers() {
        return readLines("users.dat").stream()
                .map(this::parseUser)
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> b, LinkedHashMap::new));
    }

    public Map<Long, Venue> loadVenues() {
        return readLines("venues.dat").stream()
                .map(this::parseVenue)
                .collect(Collectors.toMap(Venue::getId, v -> v, (a, b) -> b, LinkedHashMap::new));
    }

    public Map<Long, Event> loadEvents() {
        return readLines("events.dat").stream()
                .map(this::parseEvent)
                .collect(Collectors.toMap(Event::getId, e -> e, (a, b) -> b, LinkedHashMap::new));
    }

    public Map<Long, Booking> loadBookings() {
        return readLines("bookings.dat").stream()
                .map(this::parseBooking)
                .collect(Collectors.toMap(Booking::getId, b -> b, (a, bb) -> bb, LinkedHashMap::new));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Encryption helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Encrypts plaintext using AES-256-GCM.
     * A fresh random IV is generated for every call — this is critical because
     * reusing an IV with the same key in GCM mode completely breaks security.
     */
    private byte[] encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Prepend IV so we can extract it during decryption
            byte[] result = new byte[GCM_IV_LENGTH + ciphertext.length];
            System.arraycopy(iv,         0, result, 0,             GCM_IV_LENGTH);
            System.arraycopy(ciphertext, 0, result, GCM_IV_LENGTH, ciphertext.length);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts data produced by {@link #encrypt}.
     * GCM authentication tag verification happens inside {@code cipher.doFinal} —
     * any tampering with the ciphertext will throw {@link javax.crypto.AEADBadTagException}.
     */
    private String decrypt(byte[] encryptedWithIv) {
        try {
            byte[] iv         = Arrays.copyOfRange(encryptedWithIv, 0, GCM_IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(encryptedWithIv, GCM_IV_LENGTH, encryptedWithIv.length);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed — wrong key or corrupted file", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // File I/O
    // ─────────────────────────────────────────────────────────────────────────

    private void writeTo(String fileName, String content) {
        try {
            byte[] encrypted = encrypt(content);
            Files.write(folder.resolve(fileName), Base64.getEncoder().encode(encrypted));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to persist " + fileName, e);
        }
    }

    private List<String> readLines(String fileName) {
        Path path = folder.resolve(fileName);
        if (!Files.exists(path)) return new ArrayList<>();
        try {
            byte[] base64Bytes = Files.readAllBytes(path);
            if (base64Bytes.length == 0) return new ArrayList<>();
            byte[] encrypted = Base64.getDecoder().decode(base64Bytes);
            String content = decrypt(encrypted);
            if (content.isBlank()) return new ArrayList<>();
            return Arrays.asList(content.split(System.lineSeparator()));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + fileName, e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Serialisation helpers
    // ─────────────────────────────────────────────────────────────────────────

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
                        + e.getVenueId() + "," + e.getTotalSeats() + ","
                        + e.getAvailableSeats() + "," + e.getOrganizerId())
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
