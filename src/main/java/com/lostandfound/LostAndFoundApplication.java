package com.lostandfound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main Spring Boot Application Class
 * Entry point for Lost and Found Management System
 */
@SpringBootApplication
@EnableJpaAuditing
public class LostAndFoundApplication {

    public static void main(String[] args) {
        SpringApplication.run(LostAndFoundApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("Lost and Found System Started Successfully!");
        System.out.println("Access H2 Console: http://localhost:8080/h2-console");
        System.out.println("Application URL: http://localhost:8080");
        System.out.println("========================================\n");
    }
}
