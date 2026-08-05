package com.bookmyevent;

import com.bookmyevent.config.AppConfig;
import com.bookmyevent.runner.ConsoleRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Day 3 — Application entry point.
 *
 * <p><b>Before (Day 1):</b>
 * <pre>
 *   FileStorage storage        = new FileStorage("data");
 *   UserService userService    = new UserService(storage);
 *   VenueService venueService  = new VenueService(storage);
 *   EventService eventService  = new EventService(storage);
 *   BookingService booking     = new BookingService(storage, userService, eventService);
 *   // ... then manually pass every dependency to every helper method
 * </pre>
 *
 * <p><b>After (Day 3):</b>
 * <pre>
 *   // Spring reads @Configuration + @ComponentScan, builds the full object graph,
 *   // and hands us a ready-to-use ConsoleRunner with everything wired.
 *   var ctx = new AnnotationConfigApplicationContext(AppConfig.class);
 *   ctx.getBean(ConsoleRunner.class).run();
 * </pre>
 *
 * <p>The IoC container is the owner of object creation. Our code only declares
 * <em>what</em> it needs (via constructor parameters), never <em>how</em> to build it.
 */
public class App {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            ConsoleRunner runner = context.getBean(ConsoleRunner.class);
            runner.run();
        }
    }
}

