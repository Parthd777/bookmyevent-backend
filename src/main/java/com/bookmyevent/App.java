package com.bookmyevent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Day 4 — Spring Boot entry point.
 *
 * <p><b>Day 3 bootstrap:</b>
 * <pre>
 *   var ctx = new AnnotationConfigApplicationContext(AppConfig.class);
 *   ctx.getBean(ConsoleRunner.class).run();
 * </pre>
 *
 * <p><b>Day 4 bootstrap:</b>
 * <pre>
 *   SpringApplication.run(App.class, args);
 * </pre>
 *
 * <p>{@code @SpringBootApplication} is shorthand for three annotations:
 * <ul>
 *   <li>{@code @Configuration}           — this class can define @Beans</li>
 *   <li>{@code @ComponentScan}           — scans com.bookmyevent.** for @Service, @Repository, @RestController</li>
 *   <li>{@code @EnableAutoConfiguration} — reads classpath and auto-wires Tomcat, Jackson, Validation</li>
 * </ul>
 *
 * <p>Why Spring Boot over plain Spring?
 * <ul>
 *   <li>No AppConfig / XML boilerplate — convention over configuration</li>
 *   <li>Embedded Tomcat — run as {@code java -jar}, no WAR deployment needed</li>
 *   <li>Opinionated starter dependencies — version-managed and integration-tested</li>
 *   <li>application.properties — single source of truth for all config</li>
 * </ul>
 */
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
