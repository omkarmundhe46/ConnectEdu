package com.campusconnect.clubservice.controller;

import com.campusconnect.clubservice.dto.ClubMemberResponseDto;
import com.campusconnect.clubservice.dto.ClubResponseDto;
import com.campusconnect.clubservice.repository.ClubRepository;
import com.campusconnect.clubservice.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/api/clubs") // Use a different path prefix
@RequiredArgsConstructor
public class InternalController {

    private final ClubService clubService;
    private final ClubRepository clubRepository;

    @GetMapping("/{id}")
    public ClubResponseDto getClubById(@PathVariable Long id) {
        return clubService.getClubById(id);
    }

    @GetMapping("/{clubId}/members")
    public List<ClubMemberResponseDto> getClubMembers(@PathVariable Long clubId) {
        return clubService.getClubMembers(clubId);
    }

}