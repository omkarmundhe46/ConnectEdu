package com.campusconnect.userservice.service;

import com.campusconnect.userservice.config.JwtService;
import com.campusconnect.userservice.dto.*;
import com.campusconnect.userservice.entity.Role; // ADDED
 import com.campusconnect.userservice.entity.User;
import com.campusconnect.userservice.entity.VerificationToken;
import com.campusconnect.userservice.exception.EmailAlreadyExistsException;
import com.campusconnect.userservice.exception.UserNotFoundException;
import com.campusconnect.userservice.kafka.UserKafkaProducer;
import com.campusconnect.userservice.repository.UserRepository;
import com.campusconnect.userservice.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder; // ADDED
 import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository; // --- INJECT ---
    private final JwtService jwtService;
    private final UserKafkaProducer userKafkaProducer; // --- ADD THIS FIELD ---
    Logger log = org.slf4j.LoggerFactory.getLogger(UserService.class);

    private static final String DEFAULT_PROFILE_IMAGE = "https://i.imgur.com/example.png";


    // This method is now handled by AuthenticationService, but we keep the core logic
    // for other parts of the app. It's important to encode the password here as well.
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + userRequestDto.getEmail());
        }

        User user = User.builder()
                .name(userRequestDto.getName())
                .email(userRequestDto.getEmail())
                .password(passwordEncoder.encode(userRequestDto.getPassword())) // ENCODE PASSWORD
                .department(userRequestDto.getDepartment())
                .phone(null) // Set default phone
                .profileImageUrl(DEFAULT_PROFILE_IMAGE)
                .role(Role.USER) // Set default role
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponseDto(savedUser);
    }

    public AuthenticationResponse updateUserProfile(Long userId, UpdateProfileRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Update the fields from the request
        user.setPhone(request.getPhone());
        user.setProfileImageUrl(request.getProfileImageUrl());

        User savedUser = userRepository.save(user);

        // Generate a new token with the updated claims
        String jwtToken = jwtService.generateToken(savedUser);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return mapToResponseDto(user);
    }

    // --- ADD THIS NEW METHOD FOE EXCEL ---
    public List<UserResponseDto> getUsersByIds(List<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return mapToResponseDto(user);
    }

    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        
        if (!user.getEmail().equals(userRequestDto.getEmail()) && 
            userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + userRequestDto.getEmail());
        }
        
        user.setName(userRequestDto.getName());
        user.setEmail(userRequestDto.getEmail());
        user.setPassword(userRequestDto.getPassword());
        user.setDepartment(userRequestDto.getDepartment());
        
        User updatedUser = userRepository.save(user);
        return mapToResponseDto(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public String getUserName(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return user.getName();
    }

    // Update the DTO mapping to include the new role
    private UserResponseDto mapToResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setDepartment(user.getDepartment());
        dto.setRole(user.getRole()); // ADD ROLE
        dto.setManagedClubId(user.getManagedClubId()); // ADD MANAGED CLUB ID
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    public void updateUserRole(Long userId, UpdateUserRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Logic to prevent demoting a higher-level admin
        if (user.getRole() == Role.COLLEGE_ADMIN || user.getRole() == Role.CLUB_ADMIN && request.getNewRole() == Role.CLUB_MEMBER) {
            log.warn("Attempt to demote user {} from {} to {}. Action skipped.", userId, user.getRole(), request.getNewRole());
            return;
        }

        user.setRole(request.getNewRole());
        if (request.getNewRole() == Role.CLUB_ADMIN) {
            user.setManagedClubId(request.getManagedClubId());
        } else {
            // If they are being changed to something else, clear the managed club id
            user.setManagedClubId(null);
        }

        userRepository.save(user);
        log.info("Successfully updated role for user {} to {}", userId, request.getNewRole());
    }

    /**
     * Generates a 6-digit OTP.
     */
    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    /**
     * Creates or updates a verification token for a user.
     * Returns the 6-digit OTP.
     */
    @Transactional
    public String createVerificationToken(User user) {
        String otp = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10); // Token is valid for 10 minutes

        // Find existing token for this user, or create a new one
        VerificationToken token = tokenRepository.findByUser(user)
                .orElse(new VerificationToken());

        token.setUser(user);
        token.setOtp(otp);
        token.setExpiryTime(expiryTime);

        tokenRepository.save(token);
        log.info("Generated new OTP {} for user {}", otp, user.getEmail());
        return otp;
    }

     //    * Validates an OTP for a given email.

    @Transactional
    public void validateVerificationToken(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        VerificationToken token = tokenRepository.findByOtp(otp)
                .orElseThrow(() -> new RuntimeException("Invalid OTP."));

        if (!token.getUser().equals(user)) {
            log.warn("OTP mismatch for user {}", email);
            throw new RuntimeException("Invalid OTP for this user.");
        }

        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.warn("Expired OTP used for user {}", email);
            tokenRepository.delete(token); // Clean up expired token
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        user.setVerified(true);
        userRepository.save(user);
        log.info("User {} successfully verified.", email);

        tokenRepository.delete(token); // Token is used, delete it

        // --- 3. ADD THIS BLOCK ---
        // Send the "Welcome" notification AFTER verification is successful
        UserRegisteredRequest notificationRequest = new UserRegisteredRequest();
        notificationRequest.setUserId(user.getId());
        notificationRequest.setName(user.getName());
        notificationRequest.setEmail(user.getEmail());
        notificationRequest.setRequestId("user-registered-" + user.getId());
        userKafkaProducer.sendUserRegisteredNotification(notificationRequest);
        // --- END OF ADDITION ---
    }
}