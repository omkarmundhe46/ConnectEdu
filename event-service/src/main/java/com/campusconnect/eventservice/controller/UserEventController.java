package com.campusconnect.eventservice.controller;

import com.campusconnect.eventservice.dto.EventResponseDto;
import com.campusconnect.eventservice.dto.MyRegistrationResponseDto;
import com.campusconnect.eventservice.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events") // This is the new base path
@RequiredArgsConstructor
@Slf4j
public class UserEventController {

    private final EventService eventService;

    @GetMapping("/my-registrations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MyRegistrationResponseDto>> getMyRegistrations() {
        Long userId = getAuthenticatedUserId();
        List<MyRegistrationResponseDto> registrations = eventService.getRegistrationsForUser(userId);
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/upcoming")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponseDto>> getAllUpcomingEvents() {
        return ResponseEntity.ok(eventService.getAllUpcomingEvents());
    }

    // --- Helper Method (Copied from EventController) ---
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();

        Object userIdObj = jwt.getClaim("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        throw new IllegalStateException("User ID not found in token or is not a number.");
    }
}