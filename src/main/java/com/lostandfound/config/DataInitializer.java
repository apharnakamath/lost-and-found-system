package com.lostandfound.config;

import com.lostandfound.model.*;
import com.lostandfound.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Initialize Categories
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("Electronics", "Phones, laptops, tablets, etc."));
            categoryRepository.save(new Category("Clothing", "Jackets, bags, shoes, etc."));
            categoryRepository.save(new Category("Documents", "IDs, passports, certificates, etc."));
            categoryRepository.save(new Category("Accessories", "Watches, jewelry, glasses, etc."));
            categoryRepository.save(new Category("Keys", "House keys, car keys, etc."));
            categoryRepository.save(new Category("Wallets", "Wallets and purses"));
            categoryRepository.save(new Category("Books", "Textbooks, novels, etc."));
            categoryRepository.save(new Category("Sports Equipment", "Balls, rackets, etc."));
            categoryRepository.save(new Category("Others", "Miscellaneous items"));
        }

        // Initialize Locations
        if (locationRepository.count() == 0) {
            locationRepository.save(new Location("Main Library", "Central university library"));
            locationRepository.save(new Location("Student Center", "Main student activity center"));
            locationRepository.save(new Location("Cafeteria", "Main dining hall"));
            locationRepository.save(new Location("Gym", "University fitness center"));
            locationRepository.save(new Location("Parking Lot A", "North parking area"));
            locationRepository.save(new Location("Lecture Hall 1", "Main lecture building"));
            locationRepository.save(new Location("Computer Lab", "Engineering computer lab"));
            locationRepository.save(new Location("Dormitory", "Student housing"));
        }

        // Initialize Admin User
        if (userRepository.findByEmail("admin@lostandfound.com").isEmpty()) {
            Admin admin = new Admin();
            admin.setName("System Administrator");
            admin.setEmail("admin@lostandfound.com");
            admin.setPhone("1234567890");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }

        // Initialize Sample User
        if (userRepository.findByEmail("user@test.com").isEmpty()) {
            User user = new User();
            user.setName("Test User");
            user.setEmail("user@test.com");
            user.setPhone("9876543210");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole("USER");
            userRepository.save(user);
        }

        System.out.println("✓ Sample data initialized successfully!");
    }
}
