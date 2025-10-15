package com.campusconnect.discussionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "club-service", url = "${CLUB_SERVICE_URL:http://localhost:8082}")
public interface ClubClient {
    @GetMapping("/api/clubs/{clubId}/members/{userId}/check")
    Boolean checkMembership(@PathVariable("clubId") Long clubId, @PathVariable("userId") Long userId);
    
    @GetMapping("/api/clubs/{clubId}/members/{userId}/role")
    String getMemberRole(@PathVariable("clubId") Long clubId, @PathVariable("userId") Long userId);
}