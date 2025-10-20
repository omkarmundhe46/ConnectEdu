package com.campusconnect.eventservice.controller;

import com.campusconnect.eventservice.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clubs/{clubId}/events/{eventId}/participants")
@RequiredArgsConstructor
public class ExcelExportController {

    private final ExcelExportService excelExportService; // Inject the correct service

    @GetMapping("/excel")
    // CORRECTED: Only admins should be able to download participant lists.
    @PreAuthorize("hasAnyAuthority('ROLE_CLUB_ADMIN', 'ROLE_COLLEGE_ADMIN')")
    public ResponseEntity<byte[]> downloadParticipantsExcel(@PathVariable Long clubId,
                                                            @PathVariable Long eventId) throws Exception {
        // Security check for club admins
        if (isClubAdmin()) {
            validateClubOwnership(clubId);
        }

        // Delegate all logic to the service layer. The service will handle fetching
        // participants, fetching user data in bulk, and building the Excel file.
        byte[] excelFile = excelExportService.generateParticipantsExcel(clubId, eventId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=participants_event_" + eventId + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelFile);
    }

    // --- Helper Methods for Security ---

    private void validateClubOwnership(Long clubId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();

        Object managedClubIdObj = jwt.getClaim("managedClubId");
        Long managedClubId = null;
        if (managedClubIdObj instanceof Number) {
            managedClubId = ((Number) managedClubIdObj).longValue();
        }

        if (managedClubId == null || !managedClubId.equals(clubId)) {
            throw new AccessDeniedException("You are not the admin of this club.");
        }
    }

    private boolean isClubAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_CLUB_ADMIN"));
    }
}