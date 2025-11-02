package com.campusconnect.userservice.controller;

import com.campusconnect.userservice.dto.*;
import com.campusconnect.userservice.entity.User;
import com.campusconnect.userservice.kafka.UserKafkaProducer;
import com.campusconnect.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    private final UserKafkaProducer userKafkaProducer;


    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthenticationResponse> updateUserProfile(
            @Valid @RequestBody UpdateProfileRequestDto request,
            Authentication authentication
    ) {

        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        AuthenticationResponse response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyAuthority('ROLE_COLLEGE_ADMIN', 'ROLE_CLUB_ADMIN')")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
        userService.updateUserRole(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String email) {
        UserResponseDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, 
                                                     @Valid @RequestBody UserRequestDto userRequestDto) {
        UserResponseDto updatedUser = userService.updateUser(id, userRequestDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/name")
    public ResponseEntity<String> getUserName(@PathVariable Long id) {
        String userName = userService.getUserName(id);
        return ResponseEntity.ok(userName);
    }
}