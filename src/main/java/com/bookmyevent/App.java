package com.bookmyevent;

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
import com.bookmyevent.storage.FileStorage;

import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        try {
            FileStorage storage = new FileStorage("data");
            UserService userService = new UserService(storage);
            VenueService venueService = new VenueService(storage);
            EventService eventService = new EventService(storage);
            BookingService bookingService = new BookingService(storage, userService, eventService);

            Thread backgroundThread = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(10000);
                        System.out.println("[Background] Monitoring seat locks and pending bookings...");
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            backgroundThread.setDaemon(true);
            backgroundThread.start();

            Scanner scanner = new Scanner(System.in);
            boolean running = true;

            while (running) {
                printMenu();
                System.out.print("Choose an option: ");
                String choice = scanner.nextLine();

                try {
                    switch (choice) {
                        case "1":
                            registerUser(scanner, userService);
                            break;
                        case "2":
                            createVenue(scanner, venueService);
                            break;
                        case "3":
                            createEvent(scanner, eventService, venueService, userService);
                            break;
                        case "4":
                            listEvents(eventService);
                            break;
                        case "5":
                            bookSeats(scanner, bookingService, userService, eventService);
                            break;
                        case "6":
                            listBookings(bookingService);
                            break;
                        case "7":
                            System.out.println("Saving app state...");
                            storage.saveAll(userService.listUsers(), venueService.listVenues(), eventService.listEvents(), bookingService.listBookings());
                            System.out.println("State saved successfully.");
                            running = false;
                            break;
                        default:
                            System.out.println("Invalid option. Please choose again.");
                    }
                } catch (UserNotFoundException | EventFullException | BookingNotAllowedException e) {
                    System.out.println("Error: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Unexpected error: " + e.getMessage());
                }
            }

            scanner.close();
            System.out.println("Thank you for using BookMyEvent Console.");
        } catch (Exception e) {
            System.out.println("Application failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printMenu() {
        System.out.println("\n=== BookMyEvent Console v1 ===");
        System.out.println("1. Register user");
        System.out.println("2. Create venue");
        System.out.println("3. Create event");
        System.out.println("4. List events");
        System.out.println("5. Book seats");
        System.out.println("6. View bookings");
        System.out.println("7. Save and exit");
    }

    private static void registerUser(Scanner scanner, UserService userService) {
        System.out.print("Enter user name: ");
        String name = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter role (ADMIN/CUSTOMER): ");
        String role = scanner.nextLine().toUpperCase();
        User user = userService.registerUser(name, email, role);
        System.out.println("User registered: " + user);
    }

    private static void createVenue(Scanner scanner, VenueService venueService) {
        System.out.print("Enter venue name: ");
        String name = scanner.nextLine();
        System.out.print("Enter city: ");
        String city = scanner.nextLine();
        System.out.print("Enter capacity: ");
        int capacity = Integer.parseInt(scanner.nextLine());
        Venue venue = venueService.createVenue(name, city, capacity);
        System.out.println("Venue created: " + venue);
    }

    private static void createEvent(Scanner scanner, EventService eventService, VenueService venueService, UserService userService) {
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
        System.out.println("Event created: " + event);
    }

    private static void listEvents(EventService eventService) {
        System.out.println("Available events:");
        List<Event> events = eventService.listEvents();
        if (events.isEmpty()) {
            System.out.println("No events found.");
            return;
        }
        events.forEach(System.out::println);
    }

    private static void bookSeats(Scanner scanner, BookingService bookingService, UserService userService, EventService eventService) throws Exception {
        System.out.print("Enter user id: ");
        long userId = Long.parseLong(scanner.nextLine());
        System.out.print("Enter event id: ");
        long eventId = Long.parseLong(scanner.nextLine());
        System.out.print("Enter seats to book: ");
        int seats = Integer.parseInt(scanner.nextLine());

        if (userService.getUserById(userId) == null) {
            throw new UserNotFoundException("User not found with id " + userId);
        }
        if (eventService.getEventById(eventId) == null) {
            throw new BookingNotAllowedException("Event not found with id " + eventId);
        }

        Booking booking = bookingService.createBooking(userId, eventId, seats);
        System.out.println("Booking confirmed: " + booking);
    }

    private static void listBookings(BookingService bookingService) {
        System.out.println("Bookings:");
        List<Booking> bookings = bookingService.listBookings();
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        bookings.forEach(System.out::println);
    }
}
