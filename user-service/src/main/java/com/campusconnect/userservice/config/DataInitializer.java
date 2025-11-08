package com.campusconnect.userservice.config;

import com.campusconnect.userservice.entity.Role;
import com.campusconnect.userservice.entity.User;
import com.campusconnect.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if the default admin user already exists
        if (!userRepository.existsByEmail("connectedu8@gmail.com")) {
            log.info("Default College Admin not found. Creating admin user...");

            User adminUser = User.builder()
                    .name("College Admin")
                    .email("connectedu8@gmail.com")
                    // IMPORTANT: Always encode the password
                    .password(passwordEncoder.encode("connectedu8"))
                    .department("Administration")
                    .role(Role.COLLEGE_ADMIN) // Assign the highest role
                    .isVerified(true)
                    .provider("local")
                    .build();

            userRepository.save(adminUser);
            log.info("Default College Admin created successfully with email: connectedu8@gmail.com");
        } else {
            log.info("Default College Admin already exists. Skipping creation.");
        }
    }
}