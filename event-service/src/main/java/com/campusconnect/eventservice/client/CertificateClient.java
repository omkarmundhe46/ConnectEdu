package com.campusconnect.eventservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

// The URL already has the base path
@FeignClient(name = "certificate-service", url = "${CERTIFICATE_SERVICE_URL}/api/certificates")
public interface CertificateClient {

    // This method is used by your scheduler/manual trigger
    @PostMapping("/event/{eventId}/user/{userId}")
    void generateAndSend(@PathVariable("eventId") Long eventId, @PathVariable("userId") Long userId);

    // CORRECTED PATH: Removed the redundant "/api/certificates" prefix
    @GetMapping("/event/{eventId}/user/{userId}/download")
    ResponseEntity<byte[]> downloadCertificate(
            @PathVariable("eventId") Long eventId,
            @PathVariable("userId") Long userId
    );
}