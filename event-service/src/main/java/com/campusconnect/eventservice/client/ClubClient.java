package com.campusconnect.eventservice.client;

import com.campusconnect.eventservice.dto.ClubDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "club-service", url = "${CLUB_SERVICE_URL:http://localhost:8082}")
public interface ClubClient {
    @GetMapping("/api/clubs/{id}")
    ClubDto getClubById(@PathVariable("id") Long id);
}