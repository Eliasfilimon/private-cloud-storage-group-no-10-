package com.udom.securecloud.config;

import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Initializes default data on application startup.
 * Creates an initial admin user if no users exist in the database.
 * This solves the "chicken and egg" problem of needing an admin to create users.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.initial-admin.email:admin@udom.ac.tz}")
    private String initialAdminEmail;

    @Value("${app.initial-admin.password:}")
    private String initialAdminPassword;

    @Value("${app.initial-admin.first-name:System}")
    private String initialAdminFirstName;

    @Value("${app.initial-admin.last-name:Administrator}")
    private String initialAdminLastName;

    @Override
    @Transactional
    public void run(String... args) {
        // Check if any users exist
        long userCount = userRepository.count();
        
        if (userCount == 0) {
            log.info("No users found in database. Creating initial admin user...");
            createInitialAdmin();
        } else {
            log.info("Database already contains {} users. Skipping initial admin creation.", userCount);
        }
    }

    private void createInitialAdmin() {
        // Generate secure password if not provided via environment
        String password = initialAdminPassword;
        if (password == null || password.isEmpty()) {
            password = generateSecurePassword();
            log.warn("╔════════════════════════════════════════════════════════════════╗");
            log.warn("║               INITIAL ADMIN CREDENTIALS                        ║");
            log.warn("╠════════════════════════════════════════════════════════════════╣");
            log.warn("║  Email:    {}                     ║", String.format("%-40s", initialAdminEmail));
            log.warn("║  Password: {}                     ║", String.format("%-40s", password));
            log.warn("╠════════════════════════════════════════════════════════════════╣");
            log.warn("║  IMPORTANT: Change this password immediately after first login ║");
            log.warn("╚════════════════════════════════════════════════════════════════╝");
        }

        User admin = new User();
        admin.setUsername(initialAdminEmail);
        admin.setEmail(initialAdminEmail);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setFirstName(initialAdminFirstName);
        admin.setLastName(initialAdminLastName);
        admin.setFullName(initialAdminFirstName + " " + initialAdminLastName);
        admin.setRole(User.Role.ADMIN);
        admin.setDepartment("IT Administration");
        admin.setStorageQuota(10737418240L); // 10GB
        admin.setStorageUsed(0L);
        admin.setIsActive(true);
        admin.setMustChangePassword(true); // Force password change on first login
        admin.setCreatedAt(LocalDateTime.now());
        admin.setLastLogin(null);

        User savedAdmin = userRepository.save(admin);
        
        log.info("Initial admin user created successfully with ID: {}", savedAdmin.getId());
        log.info("You can now login at: http://localhost:3002 (or your frontend URL)");
    }

    private String generateSecurePassword() {
        // Generate a random 12-character password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
