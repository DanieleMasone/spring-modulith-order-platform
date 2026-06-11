package com.example.orderplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/**
 * Spring Boot entry point for the modular monolith order platform.
 */
@Modulithic(systemName = "Order Platform")
@SpringBootApplication
public class OrderPlatformApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderPlatformApplication.class, args);
    }
}
