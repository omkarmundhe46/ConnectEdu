package com.campusconnect.userservice.controller;

import com.campusconnect.userservice.dto.UpdateUserRoleRequest;
import com.campusconnect.userservice.dto.UserRequestDto;
import com.campusconnect.userservice.kafka.UserKafkaProducer; // Import Kafka producer
import com.campusconnect.userservice.dto.UserResponseDto;
import com.campusconnect.userservice.service.UserService;
//import com.campusconnect.userservice.client.NotificationClient;
import com.campusconnect.userservice.dto.UserRegisteredRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
//    private final NotificationClient notificationClient;
    private final UserKafkaProducer userKafkaProducer;

//    @PostMapping("/register")
//    public ResponseEntity<UserResponseDto> register(@RequestBody UserRequestDto userRequestDto) {
//    	UserResponseDto savedUser = userService.createUser(userRequestDto);
//
//        log.info("User created with ID: {}", savedUser.getId());
//
//        UserRegisteredRequest notificationRequest =
//                new UserRegisteredRequest();
//        notificationRequest.setUserId(savedUser.getId());
//        notificationRequest.setRequestId("user-registered-" + savedUser.getId());
//
//
//        log.info("Calling notification-service for user: {}", savedUser.getId());
//        notificationClient.notifyUserRegistered(notificationRequest);
//        log.info("Notification-service call completed for user: {}", savedUser.getId());
//
//        return ResponseEntity.ok(savedUser);
//    }

//    @PostMapping("/register")
//    public ResponseEntity<UserResponseDto> register(@RequestBody UserRequestDto userRequestDto) {
//        UserResponseDto savedUser = userService.createUser(userRequestDto);
//        log.info("User created with ID: {}", savedUser.getId());
//
//        UserRegisteredRequest notificationRequest = new UserRegisteredRequest();
//        notificationRequest.setUserId(savedUser.getId());
//        notificationRequest.setRequestId("user-registered-" + savedUser.getId());
//
//        // **MODIFIED PART**: Send notification via Kafka instead of Feign
//        log.info("Queuing notification for new user: {}", savedUser.getId());
//        userKafkaProducer.sendUserRegisteredNotification(notificationRequest);
//        log.info("Notification message queued for user: {}", savedUser.getId());
//
//        return ResponseEntity.ok(savedUser);
//    }


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