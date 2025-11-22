package com.campusconnect.userservice.controller;

import com.campusconnect.userservice.config.JwtService;
import com.campusconnect.userservice.dto.*;
import com.campusconnect.userservice.entity.User;
import com.campusconnect.userservice.kafka.UserKafkaProducer;
import com.campusconnect.userservice.repository.UserRepository;
import com.campusconnect.userservice.service.AuthenticationService;
import com.campusconnect.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final UserKafkaProducer userKafkaProducer;
    private final JwtService jwtService;
    private final UserRepository userRepository;



    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserRequestDto request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest request) {
        try {
            userService.validateVerificationToken(request.getEmail(), request.getOtp());
            return ResponseEntity.ok("User verified successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody ResendOtpRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found."));

            // Re-create a token and get the new OTP
            String otp = userService.createVerificationToken(user);

            // Send the new OTP to Kafka
            EmailVerificationRequest emailRequest = EmailVerificationRequest.builder()
                    .email(user.getEmail())
                    .name(user.getName())
                    .otp(otp)
                    .build();
            userKafkaProducer.sendEmailVerification(emailRequest);

            return ResponseEntity.ok("New verification code sent.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            userService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok("OTP sent to email.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<String> verifyResetOtp(@RequestBody VerifyOtpRequest request) {
        try {
            // Reuse VerifyOtpRequest DTO since it has email and otp fields
            userService.verifyOtpForReset(request.getEmail(), request.getOtp());
            return ResponseEntity.ok("OTP is valid.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password-with-otp")
    public ResponseEntity<String> resetPasswordWithOtp(@RequestBody ResetPasswordRequest request) {
        try {
            userService.completePasswordReset(request);
            return ResponseEntity.ok("Password reset successful.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /**
     * TEMPORARY DEVELOPMENT ENDPOINT
     * This method bypasses the password check to give you a valid token for any user.
     * REMOVE THIS BEFORE PRODUCTION.
     */
    @GetMapping("/get-token-for-dev")
    public ResponseEntity<AuthenticationResponse> getTokenForDevelopment(@RequestParam String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for token generation"));
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(AuthenticationResponse.builder().token(token).build());
    }
}