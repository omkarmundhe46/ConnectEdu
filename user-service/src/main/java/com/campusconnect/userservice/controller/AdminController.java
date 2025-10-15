package com.campusconnect.userservice.controller;

import com.campusconnect.userservice.client.ClubClient;
import com.campusconnect.userservice.dto.ClubRequestDto;
import com.campusconnect.userservice.dto.ClubResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/clubs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ClubClient clubClient;

    // ----- CLUB MANAGEMENT -----

    // Create a new club
    @PostMapping
    public ClubResponseDto createClub(@RequestBody ClubRequestDto clubRequestDto) {
        return clubClient.createClub(clubRequestDto);
    }

    // Get all clubs
    @GetMapping
    public List<ClubResponseDto> getAllClubs() {
        return clubClient.getAllClubs();
    }

    // Get club by ID
    @GetMapping("/{clubId}")
    public ClubResponseDto getClubById(@PathVariable Long clubId) {
        return clubClient.getClubById(clubId);
    }

    // Update club
    @PutMapping("/{clubId}")
    public ClubResponseDto updateClub(@PathVariable Long clubId, 
                                      @RequestBody ClubRequestDto clubRequestDto) {
        return clubClient.updateClub(clubId, clubRequestDto);
    }

    // Delete club
    @DeleteMapping("/{clubId}")
    public void deleteClub(@PathVariable Long clubId) {
        clubClient.deleteClub(clubId);
    }

    @GetMapping("/analytics")
    public String getCollegeAnalytics() {
        return "College-wide analytics (Admin only)";
    }
}
