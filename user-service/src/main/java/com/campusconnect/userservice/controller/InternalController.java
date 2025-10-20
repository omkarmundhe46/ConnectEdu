package com.campusconnect.userservice.controller;

import com.campusconnect.userservice.dto.UserResponseDto;
import com.campusconnect.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/api/users") // A dedicated path for internal calls
@RequiredArgsConstructor
public class InternalController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // --- ADD THIS NEW ENDPOINT ---
    // This allows other services to fetch details for multiple users in a single call.
    @PostMapping("/batch")
    public ResponseEntity<List<UserResponseDto>> getUsersByIds(@RequestBody List<Long> userIds) {
        List<UserResponseDto> users = userService.getUsersByIds(userIds);
        return ResponseEntity.ok(users);
    }
}