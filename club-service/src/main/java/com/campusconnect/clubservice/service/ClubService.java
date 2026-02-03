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
        club.setCategory(clubRequestDto.getCategory());

        Club savedClub = clubRepository.save(club);

        // --- AUTOMATIC ROLE PROMOTION ---
        // After creating the club, tell the user-service to promote the assigned admin.
//        UpdateUserRoleRequest roleRequest = new UpdateUserRoleRequest(
//                Role.CLUB_ADMIN,
//                savedClub.getId() // Pass the new club's ID
//        );
//        userClient.updateUserRole(savedClub.getAdminId(), roleRequest);
//
//        return mapToResponseDto(savedClub);
        try {
            UpdateUserRoleRequest roleRequest = new UpdateUserRoleRequest(Role.CLUB_ADMIN, savedClub.getId());
            userClient.updateUserRole(savedClub.getAdminId(), roleRequest);
        } catch (Exception e) {
            // Log error but don't fail creation? Or handle transaction rollback
            System.err.println("Failed to promote user role: " + e.getMessage());
        }

        // Return details with admin info fetched
        return mapToResponseDto(savedClub, true);
    }

    public List<ClubResponseDto> getAllClubs() {
        return clubRepository.findAll().stream()
                // FALSE: Do NOT fetch admin details for the list view. This makes it INSTANT.
                .map(club -> mapToResponseDto(club, false))
                .collect(Collectors.toList());
    }

    public ClubResponseDto getClubById(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ClubNotFoundException("Club not found with id: " + id));
        // TRUE: Fetch admin details because it's a single detailed view.
        return mapToResponseDto(club, true);
    }

    public ClubResponseDto updateClub(Long id, ClubRequestDto clubRequestDto) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ClubNotFoundException("Club not found with id: " + id));

        Long adminIdToSet = club.getAdminId();

        if (clubRequestDto.getAdminEmail() != null && !clubRequestDto.getAdminEmail().isEmpty()) {
            try {
                UserDto adminUser = userClient.getUserByEmail(clubRequestDto.getAdminEmail());
                adminIdToSet = adminUser.getId();

                if (!adminIdToSet.equals(club.getAdminId())) {
                    UpdateUserRoleRequest roleRequest = new UpdateUserRoleRequest(Role.CLUB_ADMIN, id);
                    userClient.updateUserRole(adminIdToSet, roleRequest);
                }
            } catch (FeignException.NotFound e) {
                throw new UserNotFoundException("User with email not found");
            }
        }

        club.setName(clubRequestDto.getName());
        club.setDescription(clubRequestDto.getDescription());
        if (clubRequestDto.getCategory() != null) {
            club.setCategory(clubRequestDto.getCategory());
        }
        club.setAdminId(adminIdToSet);
        club.setLogoUrl(clubRequestDto.getLogoUrl());

        Club updatedClub = clubRepository.save(club);
        return mapToResponseDto(updatedClub, true);
    }

    public void deleteClub(Long id) {
        if (!clubRepository.existsById(id)) {
            throw new ClubNotFoundException("Club not found with id: " + id);
        }
        clubRepository.deleteById(id);
    }

    public ClubMemberResponseDto addMemberToClub(Long clubId, ClubMemberRequestDto memberRequestDto) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("Club not found with id: " + clubId));

        UserDto user;
        try {
            user = userClient.getUserByEmail(memberRequestDto.getUserEmail());
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User not found");
        }

        if (club.getAdminId().equals(user.getId())) {
            throw new DuplicateMembershipException("User is Club Admin");
        }
        if (clubMemberRepository.existsByClubIdAndUserId(clubId, user.getId())) {
            throw new DuplicateMembershipException("User already member");
        }

        ClubMember member = new ClubMember();
        member.setClubId(clubId);
        member.setUserId(user.getId());
        member.setRole(memberRequestDto.getRole());

        ClubMember savedMember = clubMemberRepository.save(member);

        try {
            UpdateUserRoleRequest roleRequest = new UpdateUserRoleRequest(Role.CLUB_MEMBER, null);
            userClient.updateUserRole(savedMember.getUserId(), roleRequest);
        } catch (Exception e) {
            System.err.println("Failed to update user role: " + e.getMessage());
        }

        return mapToMemberResponseDto(savedMember);
    }

    public List<Long> getMyClubIds(Long userId) {
        return clubMemberRepository.findByUserId(userId).stream()
                .map(ClubMember::getClubId)
                .collect(Collectors.toList());
    }

    public List<ClubResponseDto> searchClubs(String query) {
        return clubRepository.findByNameContainingIgnoreCase(query).stream()
                // FALSE: Search results usually don't need heavy admin details either
                .map(club -> mapToResponseDto(club, false))
                .collect(Collectors.toList());
    }

    public List<ClubMemberResponseDto> getClubMembers(Long clubId) {
        if (!clubRepository.existsById(clubId)) {
            throw new ClubNotFoundException("Club not found with id: " + clubId);
        }

        List<ClubMember> members = clubMemberRepository.findByClubId(clubId);
        return members.stream().map(member -> {
            ClubMemberResponseDto dto = mapToMemberResponseDto(member);
            // Fetch user details for each member
            try {
                // Ideally, this should be a batch call, but single calls work for now
                UserDto user = userClient.getUserById(member.getUserId());
                dto.setUserName(user.getName());
            } catch (Exception e) {
                dto.setUserName("Unknown User (ID: " + member.getUserId() + ")");
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // --- removeMemberFromClub ---
    public void removeMemberFromClub(Long clubId, Long userId) {
        ClubMember member = clubMemberRepository.findByClubIdAndUserId(clubId, userId)
                .orElseThrow(() -> new MembershipNotFoundException("Membership not found for club: " + clubId + " and user: " + userId));

        // 1. Remove from club_members table
        clubMemberRepository.delete(member);

        // 2. Demote user role back to USER
        // We wrap this in a try-catch so the removal succeeds even if the user-service is down
        try {
            // Note: We pass null for managedClubId because they are now just a USER
            UpdateUserRoleRequest roleRequest = new UpdateUserRoleRequest(Role.USER, null);
            userClient.updateUserRole(userId, roleRequest);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to demote user role for user " + userId + ": " + e.getMessage());
        }
    }

    public boolean isMember(Long clubId, Long userId) {

        // 1. First, check if they are in the club_members table
        boolean isListedMember = clubMemberRepository.existsByClubIdAndUserId(clubId, userId);
        if (isListedMember) {
            return true;
        }

        // 2. If not, check if they are the admin of the club
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("Club not found with id: " + clubId));

        return club.getAdminId().equals(userId);
    }

    public String getMemberRole(Long clubId, Long userId) {
        ClubMember member = clubMemberRepository.findByClubIdAndUserId(clubId, userId)
                .orElseThrow(() -> new MembershipNotFoundException("Membership not found for club: " + clubId + " and user: " + userId));
        return member.getRole().name();
    }

    private ClubResponseDto mapToResponseDto(Club club, boolean fetchAdminDetails) {
        String adminName = "Loading...";
        String adminEmail = "";

        if (fetchAdminDetails && club.getAdminId() != null) {
            try {
                UserDto user = userClient.getUserById(club.getAdminId());
                adminName = user.getName();
                adminEmail = user.getEmail();
            } catch (Exception e) {
                adminName = "Unknown (ID: " + club.getAdminId() + ")";
            }
        } else {
            // For list views, we don't want to make 50 HTTP calls.
            adminName = "View Details";
            adminEmail = "";
        }

        return ClubResponseDto.builder()
                .id(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .category(club.getCategory())
                .logoUrl(club.getLogoUrl())
                .adminId(club.getAdminId())
                .adminName(adminName)
                .adminEmail(adminEmail)
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