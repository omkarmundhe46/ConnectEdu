package com.campusconnect.userservice.controller;

import com.campusconnect.userservice.config.JwtService;
import com.campusconnect.userservice.dto.AuthenticationResponse;
import com.campusconnect.userservice.dto.LoginRequest;
import com.campusconnect.userservice.dto.UserRequestDto;
import com.campusconnect.userservice.dto.UserResponseDto;
import com.campusconnect.userservice.repository.UserRepository;
import com.campusconnect.userservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;


    // Not for production added because postman getting error for registration and login but endpoint works.
    // Inject these two for the temporary endpoint
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