package com.campusconnect.eventservice.controller;

import com.campusconnect.eventservice.dto.AnalyticsResponseDto;
import com.campusconnect.eventservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/global")
    @PreAuthorize("hasAuthority('ROLE_COLLEGE_ADMIN')")
    public ResponseEntity<AnalyticsResponseDto> getGlobalAnalytics() {
        return ResponseEntity.ok(analyticsService.getGlobalAnalytics());
    }

    @GetMapping("/club/{clubId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLUB_ADMIN', 'ROLE_CLUB_MEMBER')")
    public ResponseEntity<AnalyticsResponseDto> getClubAnalytics(@PathVariable Long clubId) {
        return ResponseEntity.ok(analyticsService.getClubAnalytics(clubId));
    }
}