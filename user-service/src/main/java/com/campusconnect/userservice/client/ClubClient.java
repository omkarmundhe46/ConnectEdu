package com.campusconnect.userservice.client;

import com.campusconnect.userservice.dto.ClubRequestDto;
import com.campusconnect.userservice.dto.ClubResponseDto;
import com.campusconnect.userservice.dto.ClubMemberRequestDto;
import com.campusconnect.userservice.dto.ClubMemberResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@FeignClient(
    name = "club-service",
    url = "${CLUB_SERVICE_URL:http://localhost:8082}/api/clubs"
)
public interface ClubClient {

    // ----- CLUB CRUD -----
    @PostMapping
    ClubResponseDto createClub(@Valid @RequestBody ClubRequestDto clubRequestDto);

    @GetMapping
    List<ClubResponseDto> getAllClubs();

    @GetMapping("/{id}")
    ClubResponseDto getClubById(@PathVariable("id") Long id);

    @PutMapping("/{id}")
    ClubResponseDto updateClub(@PathVariable("id") Long id,
                               @Valid @RequestBody ClubRequestDto clubRequestDto);

    @DeleteMapping("/{id}")
    void deleteClub(@PathVariable("id") Long id);

    // ----- CLUB MEMBERS -----
    @PostMapping("/{clubId}/members")
    ClubMemberResponseDto addMember(@PathVariable("clubId") Long clubId,
                                    @Valid @RequestBody ClubMemberRequestDto request);

    @GetMapping("/{clubId}/members")
    List<ClubMemberResponseDto> getMembers(@PathVariable("clubId") Long clubId);

    @DeleteMapping("/{clubId}/members/{userId}")
    void removeMember(@PathVariable("clubId") Long clubId,
                      @PathVariable("userId") Long userId);

    @GetMapping("/{clubId}/members/{userId}/role")
    String getMemberRole(@PathVariable("clubId") Long clubId,
                         @PathVariable("userId") Long userId);

    @GetMapping("/{clubId}/members/{userId}/check")
    Boolean checkMembership(@PathVariable("clubId") Long clubId,
                            @PathVariable("userId") Long userId);
}
