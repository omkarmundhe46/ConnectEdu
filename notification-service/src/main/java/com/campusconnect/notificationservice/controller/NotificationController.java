package com.campusconnect.notificationservice.controller;
import com.campusconnect.notificationservice.dto.CertificateNotificationRequest;
import com.campusconnect.notificationservice.dto.NotificationLogResponseDto;
import com.campusconnect.notificationservice.service.NotificationService;
import org.springframework.security.oauth2.jwt.Jwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

	private final NotificationService notificationService;

	@PostMapping("/certificate-issued")
    public ResponseEntity<String> sendCertificate(@RequestBody CertificateNotificationRequest request) {
		notificationService.sendCertificateEmail(request);
        return ResponseEntity.ok("Certificate email queued/sent to " + request.getUserEmail());
    }

    @GetMapping("/my-notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationLogResponseDto>> getMyNotifications(Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        List<NotificationLogResponseDto> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Object userIdObj = jwt.getClaim("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        throw new IllegalStateException("User ID not found in token or is not a number.");
    }



}
