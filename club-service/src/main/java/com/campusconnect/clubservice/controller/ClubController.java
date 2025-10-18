package com.campusconnect.clubservice.controller;

import com.campusconnect.clubservice.dto.ClubRequestDto;
import com.campusconnect.clubservice.dto.ClubResponseDto;
import com.campusconnect.clubservice.dto.ClubMemberAddedRequest;
import com.campusconnect.clubservice.dto.ClubMemberRequestDto;
import com.campusconnect.clubservice.dto.ClubMemberResponseDto;
import com.campusconnect.clubservice.service.ClubService;
import com.campusconnect.clubservice.kafka.ClubKafkaProducer; // Import Kafka producer
//import com.campusconnect.clubservice.client.NotificationClient;
import com.campusconnect.clubservice.dto.EmailSendRequest;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;
@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Slf4j
public class ClubController {

	private final ClubService clubService;
	private final ClubKafkaProducer clubKafkaProducer; // ADD THIS
//	private final NotificationClient notificationClient;

	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_COLLEGE_ADMIN')") // Only College Admin can create
	public ResponseEntity<ClubResponseDto> createClub(@Valid @RequestBody ClubRequestDto clubRequestDto) {
		ClubResponseDto createdClub = clubService.createClub(clubRequestDto);
		return new ResponseEntity<>(createdClub, HttpStatus.CREATED);
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()") // Any authenticated user can view clubs
	public ResponseEntity<List<ClubResponseDto>> getAllClubs() {
		List<ClubResponseDto> clubs = clubService.getAllClubs();
		return ResponseEntity.ok(clubs);
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()") // Any authenticated user can view a single club
	public ResponseEntity<ClubResponseDto> getClubById(@PathVariable Long id) {
		ClubResponseDto club = clubService.getClubById(id);
		return ResponseEntity.ok(club);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ROLE_COLLEGE_ADMIN', 'ROLE_CLUB_ADMIN')")
	public ResponseEntity<ClubResponseDto> updateClub(@PathVariable Long id, @Valid @RequestBody ClubRequestDto clubRequestDto) {
		// Fine-grained check: Only the assigned Club Admin can update their own club
		if (isClubAdmin()) {
			validateClubOwnership(id);
		}
		ClubResponseDto updatedClub = clubService.updateClub(id, clubRequestDto);
		return ResponseEntity.ok(updatedClub);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('ROLE_COLLEGE_ADMIN')") // Only College Admin can delete
	public ResponseEntity<Void> deleteClub(@PathVariable Long id) {
		clubService.deleteClub(id);
		return ResponseEntity.noContent().build();
	}
//	@PostMapping("/{clubId}/members")
//	public ResponseEntity<ClubMemberResponseDto> addMemberToClub(
//	        @PathVariable Long clubId,
//	        @Valid @RequestBody ClubMemberRequestDto memberRequestDto) {
//
//	    ClubMemberResponseDto member = clubService.addMemberToClub(clubId, memberRequestDto);
//
//	    try {
//	        ClubResponseDto club = clubService.getClubById(clubId);
//
//	        ClubMemberAddedRequest req = ClubMemberAddedRequest.builder()
//	                .userId(member.getUserId())
//	                .clubId(clubId)
//	                .role(member.getRole().name())  // ✅ enum → String
//	                .requestId("club-member-" + member.getUserId() + "-" + clubId)
//	                .variables(Map.of(
//	                        "clubName", club.getName(),
//	                        "role", member.getRole().name()
//	                ))
//	                .build();
//
//	        notificationClient.notifyClubMemberAdded(req);
//	    } catch (Exception e) {
//	        log.error("❌ Failed to send club member notification", e);
//	    }
//
//	    return new ResponseEntity<>(member, HttpStatus.CREATED);
//	}

	@PostMapping("/{clubId}/members")
	@PreAuthorize("hasAuthority('ROLE_CLUB_ADMIN')") // Only Club Admin can add members
	public ResponseEntity<ClubMemberResponseDto> addMemberToClub(
			@PathVariable Long clubId,
			@Valid @RequestBody ClubMemberRequestDto memberRequestDto) {
		validateClubOwnership(clubId); // Check if they are the admin of THIS club
		ClubMemberResponseDto member = clubService.addMemberToClub(clubId, memberRequestDto);

		try {
			// **MODIFIED PART**: Send notification via Kafka
			ClubMemberAddedRequest req = ClubMemberAddedRequest.builder()
					.userId(member.getUserId())
					.clubId(clubId)
					.role(member.getRole().name())
					.build();

			clubKafkaProducer.sendClubMemberAddedNotification(req);

		} catch (Exception e) {
			log.error("❌ Failed to queue club member notification", e);
		}

		return new ResponseEntity<>(member, HttpStatus.CREATED);
	}

	// --- HELPER METHODS FOR SECURITY CHECKS ---
	private void validateClubOwnership(Long clubId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Jwt jwt = (Jwt) authentication.getPrincipal();
		Long managedClubId = jwt.getClaim("managedClubId");

		if (managedClubId == null || !managedClubId.equals(clubId)) {
			throw new AccessDeniedException("You are not the admin of this club.");
		}
	}

	private boolean isClubAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getAuthorities().stream()
				.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_CLUB_ADMIN"));
	}




	@GetMapping("/{clubId}/members")
	@PreAuthorize("hasAnyAuthority('ROLE_COLLEGE_ADMIN', 'ROLE_CLUB_ADMIN')") // College Admin or Club Admin
	public ResponseEntity<List<ClubMemberResponseDto>> getClubMembers(@PathVariable Long clubId) {
		// Fine-grained check: if the user is a Club Admin, they must own this club
		if (isClubAdmin()) {
			validateClubOwnership(clubId);
		}
		List<ClubMemberResponseDto> members = clubService.getClubMembers(clubId);
		return ResponseEntity.ok(members);
	}

// ---

	@DeleteMapping("/{clubId}/members/{userId}")
	@PreAuthorize("hasAnyAuthority('ROLE_COLLEGE_ADMIN', 'ROLE_CLUB_ADMIN')") // College Admin or Club Admin
	public ResponseEntity<Void> removeMemberFromClub(@PathVariable Long clubId, @PathVariable Long userId) {
		// Fine-grained check: if the user is a Club Admin, they must own this club
		if (isClubAdmin()) {
			validateClubOwnership(clubId);
		}
		clubService.removeMemberFromClub(clubId, userId);
		return ResponseEntity.noContent().build();
	}

// ---

	@GetMapping("/{clubId}/members/{userId}/check")
	@PreAuthorize("isAuthenticated()") // Any authenticated user can check membership
	public ResponseEntity<Boolean> checkMembership(@PathVariable Long clubId, @PathVariable Long userId) {
		boolean isMember = clubService.isMember(clubId, userId);
		return ResponseEntity.ok(isMember);
	}

// ---

	@GetMapping("/{clubId}/members/{userId}/role")
	@PreAuthorize("hasAnyAuthority('ROLE_COLLEGE_ADMIN', 'ROLE_CLUB_ADMIN')") // College Admin or Club Admin
	public ResponseEntity<String> getMemberRole(@PathVariable Long clubId, @PathVariable Long userId) {
		// Fine-grained check: if the user is a Club Admin, they must own this club
		if (isClubAdmin()) {
			validateClubOwnership(clubId);
		}
		String role = clubService.getMemberRole(clubId, userId);
		return ResponseEntity.ok(role);
	}
}