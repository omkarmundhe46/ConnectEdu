package com.campusconnect.userservice.service;

import com.campusconnect.userservice.config.JwtService;
import com.campusconnect.userservice.dto.AuthenticationResponse;
import com.campusconnect.userservice.dto.LoginRequest;
import com.campusconnect.userservice.dto.UserRequestDto;
import com.campusconnect.userservice.dto.UserResponseDto;
import com.campusconnect.userservice.dto.UserRegisteredRequest;
import com.campusconnect.userservice.entity.Role;
import com.campusconnect.userservice.entity.User;
import com.campusconnect.userservice.exception.EmailAlreadyExistsException;
import com.campusconnect.userservice.kafka.UserKafkaProducer;
import com.campusconnect.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserKafkaProducer userKafkaProducer;
    private static final String DEFAULT_PROFILE_IMAGE = "https://i.imgur.com/example.png";

    public UserResponseDto register(UserRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .department(request.getDepartment())
                .role(Role.USER)
                .phone(null)
                .profileImageUrl(DEFAULT_PROFILE_IMAGE)
                .build();
        User savedUser = userRepository.save(user);

        // Create the notification request AND populate all the necessary fields.
        UserRegisteredRequest notificationRequest = new UserRegisteredRequest();
        notificationRequest.setUserId(savedUser.getId());
        notificationRequest.setName(savedUser.getName());
        notificationRequest.setEmail(savedUser.getEmail());
        notificationRequest.setRequestId("user-registered-" + savedUser.getId());

        // Send the complete DTO to Kafka
        userKafkaProducer.sendUserRegisteredNotification(notificationRequest);

        // Return a response DTO (create one if you don't have it)
        return UserResponseDto.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .department(savedUser.getDepartment())
                .role(savedUser.getRole())
                .phone(savedUser.getPhone())
                .profileImageUrl(savedUser.getProfileImageUrl())
                .build();
    }

    public AuthenticationResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }
}