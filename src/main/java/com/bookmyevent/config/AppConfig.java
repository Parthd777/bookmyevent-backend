package com.bookmyevent.config;

import com.bookmyevent.storage.FileStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Day 3 — Spring Core Configuration
 *
 * <p>Before (Day 1): objects were created manually with {@code new} inside App.java:
 * <pre>
 *   FileStorage storage     = new FileStorage("data");
 *   UserService userService = new UserService(storage);
 *   ...
 * </pre>
 *
 * <p>After (Day 3): Spring's IoC container reads this class at startup and
 * creates + wires all beans automatically.
 *
 * <p>Why constructor injection (preferred over @Autowired on fields)?
 * <ul>
 *   <li>Makes dependencies explicit and immutable (final fields)</li>
 *   <li>Easy to test — just pass mocks in the constructor</li>
 *   <li>Fails fast at startup if a dependency is missing</li>
 * </ul>
 */
@Configuration
@ComponentScan("com.bookmyevent")  // tells Spring where to look for @Service, @Component, etc.
public class AppConfig {

    /**
     * FileStorage is declared as a @Bean (not @Repository with component scan)
     * because its constructor throws a checked IOException that needs to be handled.
     *
     * <p>Spring will manage this as a singleton — the same FileStorage instance
     * is injected into every service that needs it.
     */
    @Bean
    public FileStorage fileStorage() throws IOException {
        return new FileStorage("data");
    }
}
