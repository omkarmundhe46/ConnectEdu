package com.campusconnect.userservice.controller;

import com.campusconnect.userservice.entity.Role;
import com.campusconnect.userservice.entity.User;
import com.campusconnect.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            return "User already exists!";
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(role)
                .build();

        userRepository.save(user);
        return "User registered successfully!";
    }

    @GetMapping("/success")
    public String loginSuccess() {
        return "Login successful with Google or Email!";
    }
}