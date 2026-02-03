package com.campusconnect.clubservice.controller;

import com.campusconnect.clubservice.dto.ChatClubDto;
import com.campusconnect.clubservice.entity.Club;
import com.campusconnect.clubservice.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/chat/clubs")
@RequiredArgsConstructor
public class ClubChatController {

    private final ClubRepository clubRepository;

    @GetMapping("/all")
    public List<ChatClubDto> getAllClubs() {
        return clubRepository.findAll().stream()
                .map(club -> ChatClubDto.builder()
                        .id(club.getId()) // <--- ADD THIS
                        .name(club.getName())
                        .description(club.getDescription())
                        // This generates email without calling user-service (Fast!)
                        .adminEmail("admin@" + club.getName().toLowerCase().replace(" ", "") + ".com")
                        .build())
                .collect(Collectors.toList());
    }
}