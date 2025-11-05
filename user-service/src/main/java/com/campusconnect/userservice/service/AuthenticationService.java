package com.campusconnect.userservice.service;

import com.campusconnect.userservice.config.JwtService;
import com.campusconnect.userservice.dto.*;
import com.campusconnect.userservice.entity.Role;
import com.campusconnect.userservice.entity.User;
import com.campusconnect.userservice.exception.EmailAlreadyExistsException;
import com.campusconnect.userservice.kafka.UserKafkaProducer;
import com.campusconnect.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
    private final UserService userService;
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
                .isVerified(false)
                .provider("local")
                .build();
        User savedUser = userRepository.save(user);

        // 1. Create an OTP for the user
        String otp = userService.createVerificationToken(savedUser);

        // 2. Send the OTP to the email-verification-topic
        EmailVerificationRequest emailRequest = EmailVerificationRequest.builder()
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .otp(otp)
                .build();
        userKafkaProducer.sendEmailVerification(emailRequest);

        // 4. Return the user DTO, but NO token.
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

        if (!"local".equals(user.getProvider())) {
            throw new BadCredentialsException("This account is registered with Google. Please use Google login.");
        }

        if (!user.isVerified()) {
            throw new BadCredentialsException("User is not verified. Please check your email for a verification code.");
        }


        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }
}