package com.bookmyevent.runner;

import com.bookmyevent.exception.BookingNotAllowedException;
import com.bookmyevent.exception.EventFullException;
import com.bookmyevent.exception.UserNotFoundException;
import com.bookmyevent.model.Booking;
import com.bookmyevent.model.Event;
import com.bookmyevent.model.User;
import com.bookmyevent.model.Venue;
import com.bookmyevent.service.BookingService;
import com.bookmyevent.service.EventService;
import com.bookmyevent.service.UserService;
import com.bookmyevent.service.VenueService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

/**
 * Day 3 — Console input/output layer (equivalent of a controller in a web app).
 *
 * <p>On Day 1, this entire menu logic lived in static methods inside {@code App.java}.
 * Static methods cannot be managed by the Spring container, cannot be injected, and
 * cannot be tested in isolation.
 *
 * <p>Extracting it into a {@code @Component} gives us:
 * <ul>
 *   <li>Spring manages the lifecycle of this object</li>
 *   <li>All service dependencies are injected via constructor — no {@code new} keyword</li>
 *   <li>The class can be tested by passing mock services in the constructor</li>
 * </ul>
 *
 * <p>Architecture layer analogy:
 * <pre>
 *   ConsoleRunner    ≈  @Controller   (input/output boundary)
 *   *Service         ≈  @Service      (business logic)
 *   FileStorage      ≈  @Repository   (data access)
 * </pre>
 *
 * <p>On Day 4 this class will be replaced by REST controllers annotated with
 * {@code @RestController}, and the scanner will be replaced by HTTP request/response.
 */
@Component
public class ConsoleRunner implements Runnable {

    private final UserService userService;
    private final VenueService venueService;
    private final EventService eventService;
    private final BookingService bookingService;

    // Constructor injection: Spring resolves each @Service bean and passes it here.
    public ConsoleRunner(UserService userService,
                         VenueService venueService,
                         EventService eventService,
                         BookingService bookingService) {
        this.userService = userService;
        this.venueService = venueService;
        this.eventService = eventService;
        this.bookingService = bookingService;
    }

    @Override
    public void run() {
        startBackgroundMonitor();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1" -> registerUser(scanner);
                    case "2" -> createVenue(scanner);
                    case "3" -> createEvent(scanner);
                    case "4" -> listEvents();
                    case "5" -> bookSeats(scanner);
                    case "6" -> listBookings();
                    case "7" -> running = false;
                    default  -> System.out.println("Invalid option. Please choose again.");
                }
            } catch (UserNotFoundException | EventFullException | BookingNotAllowedException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number: " + e.getMessage());
            }
        }

        scanner.close();
        System.out.println("Thank you for using BookMyEvent Console.");
    }

    // -------------------------------------------------------------------------
    // Private menu handlers
    // -------------------------------------------------------------------------

    private void registerUser(Scanner scanner) {
        System.out.print("Enter user name: ");
        String name = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter role (ADMIN/CUSTOMER): ");
        String role = scanner.nextLine().toUpperCase();
        User user = userService.registerUser(name, email, role);
        System.out.println("Registered: " + user);
    }

    private void createVenue(Scanner scanner) {
        System.out.print("Enter venue name: ");
        String name = scanner.nextLine();
        System.out.print("Enter city: ");
        String city = scanner.nextLine();
        System.out.print("Enter capacity: ");
        int capacity = Integer.parseInt(scanner.nextLine());
        Venue venue = venueService.createVenue(name, city, capacity);
        System.out.println("Created: " + venue);
    }

    private void createEvent(Scanner scanner) {
        System.out.print("Enter event name: ");
        String name = scanner.nextLine();
        System.out.print("Enter event date (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        System.out.print("Enter venue id: ");
        long venueId = Long.parseLong(scanner.nextLine());
        System.out.print("Enter total seats: ");
        int totalSeats = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter organizer user id: ");
        long organizerId = Long.parseLong(scanner.nextLine());

        if (venueService.getVenueById(venueId) == null) {
            System.out.println("Venue not found.");
            return;
        }
        if (userService.getUserById(organizerId) == null) {
            System.out.println("Organizer not found.");
            return;
        }

        Event event = eventService.createEvent(name, date, venueId, totalSeats, organizerId);
        System.out.println("Created: " + event);
    }

    private void listEvents() {
        List<Event> events = eventService.listEvents();
        if (events.isEmpty()) {
            System.out.println("No events found.");
            return;
        }
        System.out.println("Available events:");
        events.forEach(System.out::println);
    }

    private void bookSeats(Scanner scanner) {
        System.out.print("Enter user id: ");
        long userId = Long.parseLong(scanner.nextLine());
        System.out.print("Enter event id: ");
        long eventId = Long.parseLong(scanner.nextLine());
        System.out.print("Enter seats to book: ");
        int seats = Integer.parseInt(scanner.nextLine());
        Booking booking = bookingService.createBooking(userId, eventId, seats);
        System.out.println("Booking confirmed: " + booking);
    }

    private void listBookings() {
        List<Booking> bookings = bookingService.listBookings();
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        System.out.println("Bookings:");
        bookings.forEach(System.out::println);
    }

    private void printMenu() {
        System.out.println("\n=== BookMyEvent Console v3 (Spring Core) ===");
        System.out.println("1. Register user");
        System.out.println("2. Create venue");
        System.out.println("3. Create event");
        System.out.println("4. List events");
        System.out.println("5. Book seats");
        System.out.println("6. View bookings");
        System.out.println("7. Exit");
    }

    /**
     * Background thread simulating seat-lock expiry monitoring.
     * Carried over from Day 1 to show threading still works inside a Spring @Component.
     * On Day 7 this will become a {@code @Scheduled} job.
     */
    private void startBackgroundMonitor() {
        Thread monitor = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(10_000);
                    System.out.println("[Background] Checking for expired seat locks...");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }
}
