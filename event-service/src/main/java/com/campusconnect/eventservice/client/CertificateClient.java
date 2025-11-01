package com.campusconnect.eventservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "certificate-service", url = "${CERTIFICATE_SERVICE_URL}/api/certificates")
public interface CertificateClient {
    @PostMapping("/event/{eventId}/user/{userId}")
    void generateAndSend(@PathVariable("eventId") Long eventId, @PathVariable("userId") Long userId);

    @GetMapping("/event/{eventId}/user/{userId}/download")
    ResponseEntity<byte[]> downloadCertificate(
            @PathVariable("eventId") Long eventId,
            @PathVariable("userId") Long userId
    );
}