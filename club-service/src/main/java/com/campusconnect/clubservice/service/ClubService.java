package com.campusconnect.clubservice.service;

import com.campusconnect.clubservice.dto.ClubRequestDto;
import com.campusconnect.clubservice.dto.ClubResponseDto;
import com.campusconnect.clubservice.dto.ClubMemberRequestDto;
import com.campusconnect.clubservice.dto.ClubMemberResponseDto;
import com.campusconnect.clubservice.entity.Club;
import com.campusconnect.clubservice.entity.ClubMember;
import com.campusconnect.clubservice.exception.ClubNameAlreadyExistsException;
import com.campusconnect.clubservice.exception.ClubNotFoundException;
import com.campusconnect.clubservice.exception.DuplicateMembershipException;
import com.campusconnect.clubservice.exception.MembershipNotFoundException;
import com.campusconnect.clubservice.exception.UserNotFoundException;
import com.campusconnect.clubservice.repository.ClubRepository;
import com.campusconnect.clubservice.repository.ClubMemberRepository;
import com.campusconnect.clubservice.client.UserClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubService {
    
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final UserClient userClient;

    public ClubResponseDto createClub(ClubRequestDto clubRequestDto) {
        if (clubRepository.existsByName(clubRequestDto.getName())) {
            throw new ClubNameAlreadyExistsException("Club name already exists: " + clubRequestDto.getName());
        }
        
        Club club = new Club();
        club.setName(clubRequestDto.getName());
        club.setDescription(clubRequestDto.getDescription());
        
        Club savedClub = clubRepository.save(club);
        return mapToResponseDto(savedClub);
    }

    public List<ClubResponseDto> getAllClubs() {
        return clubRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public ClubResponseDto getClubById(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ClubNotFoundException("Club not found with id: " + id));
        return mapToResponseDto(club);
    }

    public ClubResponseDto updateClub(Long id, ClubRequestDto clubRequestDto) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ClubNotFoundException("Club not found with id: " + id));
        
        if (!club.getName().equals(clubRequestDto.getName()) && 
            clubRepository.existsByName(clubRequestDto.getName())) {
            throw new ClubNameAlreadyExistsException("Club name already exists: " + clubRequestDto.getName());
        }
        
        club.setName(clubRequestDto.getName());
        club.setDescription(clubRequestDto.getDescription());
        
        Club updatedClub = clubRepository.save(club);
        return mapToResponseDto(updatedClub);
    }

    public void deleteClub(Long id) {
        if (!clubRepository.existsById(id)) {
            throw new ClubNotFoundException("Club not found with id: " + id);
        }
        clubRepository.deleteById(id);
    }

    public ClubMemberResponseDto addMemberToClub(Long clubId, ClubMemberRequestDto memberRequestDto) {
        if (!clubRepository.existsById(clubId)) {
            throw new ClubNotFoundException("Club not found with id: " + clubId);
        }
        
        // Validate user exists
        try {
            userClient.getUserById(memberRequestDto.getUserId());
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User not found with id: " + memberRequestDto.getUserId());
        }
        
        if (clubMemberRepository.existsByClubIdAndUserId(clubId, memberRequestDto.getUserId())) {
            throw new DuplicateMembershipException("User already member of this club");
        }
        
        ClubMember member = new ClubMember();
        member.setClubId(clubId);
        member.setUserId(memberRequestDto.getUserId());
        member.setRole(memberRequestDto.getRole());
        
        ClubMember savedMember = clubMemberRepository.save(member);
        return mapToMemberResponseDto(savedMember);
    }

    public List<ClubMemberResponseDto> getClubMembers(Long clubId) {
        if (!clubRepository.existsById(clubId)) {
            throw new ClubNotFoundException("Club not found with id: " + clubId);
        }
        
        return clubMemberRepository.findByClubId(clubId).stream()
                .map(this::mapToMemberResponseDto)
                .collect(Collectors.toList());
    }

    public void removeMemberFromClub(Long clubId, Long userId) {
        ClubMember member = clubMemberRepository.findByClubIdAndUserId(clubId, userId)
                .orElseThrow(() -> new MembershipNotFoundException("Membership not found for club: " + clubId + " and user: " + userId));
        
        clubMemberRepository.delete(member);
    }

    public boolean isMember(Long clubId, Long userId) {
        return clubMemberRepository.existsByClubIdAndUserId(clubId, userId);
    }

    public String getMemberRole(Long clubId, Long userId) {
        ClubMember member = clubMemberRepository.findByClubIdAndUserId(clubId, userId)
                .orElseThrow(() -> new MembershipNotFoundException("Membership not found for club: " + clubId + " and user: " + userId));
        return member.getRole().name();
    }

    private ClubResponseDto mapToResponseDto(Club club) {
        ClubResponseDto dto = new ClubResponseDto();
        dto.setId(club.getId());
        dto.setName(club.getName());
        dto.setDescription(club.getDescription());
        dto.setCreatedAt(club.getCreatedAt());
        dto.setUpdatedAt(club.getUpdatedAt());
        return dto;
    }

    private ClubMemberResponseDto mapToMemberResponseDto(ClubMember member) {
        ClubMemberResponseDto dto = new ClubMemberResponseDto();
        dto.setId(member.getId());
        dto.setUserId(member.getUserId());
        dto.setClubId(member.getClubId());
        dto.setRole(member.getRole());
        dto.setJoinedAt(member.getJoinedAt());
        return dto;
    }
}