package com.campusconnect.eventservice.client;

import com.campusconnect.eventservice.dto.ClubDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "club-service")
public interface ClubClient {
    @GetMapping("/api/clubs/{id}")
    ClubDto getClubById(@PathVariable("id") Long id);

    // ADD THIS NEW METHOD
    @GetMapping("/api/clubs/{clubId}/members/{userId}/check")
    boolean isMember(@PathVariable("clubId") Long clubId, @PathVariable("userId") Long userId);
}