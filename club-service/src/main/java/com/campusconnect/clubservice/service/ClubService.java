package com.campusconnect.clubservice.service;

import com.campusconnect.clubservice.dto.*;
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
// ... other imports
import com.campusconnect.clubservice.entity.Role; // Assuming you have a Role enum here too
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

        UserDto adminUser;
        try {
            adminUser = userClient.getUserByEmail(clubRequestDto.getAdminEmail());
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User with email not found: " + clubRequestDto.getAdminEmail());
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify admin user: " + e.getMessage());
        }

        Club club = new Club();
        club.setName(clubRequestDto.getName());
        club.setDescription(clubRequestDto.getDescription());
        club.setAdminId(adminUser.getId()); // Use the ID found via email
        club.setLogoUrl(clubRequestDto.getLogoUrl()); // Set logo URL

        Club savedClub = clubRepository.save(club);

        // --- AUTOMATIC ROLE PROMOTION ---
        // After creating the club, tell the user-service to promote the assigned admin.
        UpdateUserRoleRequest roleRequest = new UpdateUserRoleRequest(
                Role.CLUB_ADMIN,
                savedClub.getId() // Pass the new club's ID
        );
        userClient.updateUserRole(savedClub.getAdminId(), roleRequest);

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

        // --- CHANGE: Find user by email (if email is provided for update) ---
        Long adminIdToSet = club.getAdminId(); // Keep existing admin by default
        if (clubRequestDto.getAdminEmail() != null && !clubRequestDto.getAdminEmail().isEmpty()) {
            try {
                UserDto adminUser = userClient.getUserByEmail(clubRequestDto.getAdminEmail());
                adminIdToSet = adminUser.getId();

                // Promote the NEW admin if they are different
                if (!adminIdToSet.equals(club.getAdminId())) {
                    UpdateUserRoleRequest roleRequest = new UpdateUserRoleRequest(Role.CLUB_ADMIN, id);
                    userClient.updateUserRole(adminIdToSet, roleRequest);
                    // Consider demoting the old admin if needed? (More complex logic)
                }

            } catch (FeignException.NotFound e) {
                throw new UserNotFoundException("User with email not found: " + clubRequestDto.getAdminEmail());
            } catch (Exception e) {
                throw new RuntimeException("Failed to verify admin user: " + e.getMessage());
            }
        }
        
        club.setName(clubRequestDto.getName());
        club.setDescription(clubRequestDto.getDescription());
        club.setAdminId(adminIdToSet); // Set potentially updated adminId
        club.setLogoUrl(clubRequestDto.getLogoUrl()); // Update logo URL
        
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
        // 1. Validate user exists by email
        UserDto user;
        try {
            user = userClient.getUserByEmail(memberRequestDto.getUserEmail());
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User not found with email: " + memberRequestDto.getUserEmail());
        }

        // 2. Use the found user's ID for checks and saving
        if (clubMemberRepository.existsByClubIdAndUserId(clubId, user.getId())) {
            throw new DuplicateMembershipException("User already member of this club");
        }

        ClubMember member = new ClubMember();
        member.setClubId(clubId);
        member.setUserId(user.getId());
        member.setRole(memberRequestDto.getRole());

        ClubMember savedMember = clubMemberRepository.save(member);

        // --- AUTOMATIC ROLE PROMOTION ---
        UpdateUserRoleRequest roleRequest = new UpdateUserRoleRequest(Role.CLUB_MEMBER, null);
        userClient.updateUserRole(savedMember.getUserId(), roleRequest);

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

    // Update the mapping method to include adminId
    private ClubResponseDto mapToResponseDto(Club club) {
        return ClubResponseDto.builder()
                .id(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .adminId(club.getAdminId())
                .logoUrl(club.getLogoUrl()) // Map logo URL
                .createdAt(club.getCreatedAt())
                .updatedAt(club.getUpdatedAt())
                .build();
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