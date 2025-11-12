package com.campusconnect.notificationservice.feign;

import com.campusconnect.notificationservice.dto.ClubMemberDto;
import com.campusconnect.notificationservice.dto.ClubResponseDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "club-service")
public interface ClubClient {
    @GetMapping("/{clubId}/members")
    List<ClubMemberDto> getClubMembers(@PathVariable("clubId") Long clubId);

    @GetMapping("/{clubId}")
    ClubResponseDto getClubById(@PathVariable("clubId") Long clubId);
}