package com.campusconnect.discussionservice.controller;

import com.campusconnect.discussionservice.service.S3StorageService; // Assuming S3 service is here
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map; // Import Map

@RestController
@RequestMapping("/api/uploads") // New base path
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private final S3StorageService s3StorageService;

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()") // Allow any logged-in user
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File cannot be empty"));
        }
        try {
            String fileUrl = s3StorageService.uploadFile(file);
            log.info("File uploaded successfully via /api/uploads: {}", fileUrl);

            return ResponseEntity.ok(Map.of("fileUrl", fileUrl));
        } catch (Exception e) {
            log.error("File upload failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "File upload failed: " + e.getMessage()));
        }
    }
}