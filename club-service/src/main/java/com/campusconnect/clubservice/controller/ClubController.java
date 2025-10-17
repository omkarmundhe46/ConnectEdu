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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Slf4j
public class ClubController {

	private final ClubService clubService;
	private final ClubKafkaProducer clubKafkaProducer; // ADD THIS
//	private final NotificationClient notificationClient;

	@PostMapping
	public ResponseEntity<ClubResponseDto> createClub(@Valid @RequestBody ClubRequestDto clubRequestDto) {
		ClubResponseDto createdClub = clubService.createClub(clubRequestDto);
		return new ResponseEntity<>(createdClub, HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<ClubResponseDto>> getAllClubs() {
		List<ClubResponseDto> clubs = clubService.getAllClubs();
		return ResponseEntity.ok(clubs);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ClubResponseDto> getClubById(@PathVariable Long id) {
		ClubResponseDto club = clubService.getClubById(id);
		return ResponseEntity.ok(club);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ClubResponseDto> updateClub(@PathVariable Long id,
			@Valid @RequestBody ClubRequestDto clubRequestDto) {
		ClubResponseDto updatedClub = clubService.updateClub(id, clubRequestDto);
		return ResponseEntity.ok(updatedClub);
	}

	@DeleteMapping("/{id}")
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
	public ResponseEntity<ClubMemberResponseDto> addMemberToClub(
			@PathVariable Long clubId,
			@Valid @RequestBody ClubMemberRequestDto memberRequestDto) {

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

	@GetMapping("/{clubId}/members")
	public ResponseEntity<List<ClubMemberResponseDto>> getClubMembers(@PathVariable Long clubId) {
		List<ClubMemberResponseDto> members = clubService.getClubMembers(clubId);
		return ResponseEntity.ok(members);
	}

	@DeleteMapping("/{clubId}/members/{userId}")
	public ResponseEntity<Void> removeMemberFromClub(@PathVariable Long clubId, @PathVariable Long userId) {
		clubService.removeMemberFromClub(clubId, userId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{clubId}/members/{userId}/check")
	public ResponseEntity<Boolean> checkMembership(@PathVariable Long clubId, @PathVariable Long userId) {
		boolean isMember = clubService.isMember(clubId, userId);
		return ResponseEntity.ok(isMember);
	}

	@GetMapping("/{clubId}/members/{userId}/role")
	public ResponseEntity<String> getMemberRole(@PathVariable Long clubId, @PathVariable Long userId) {
		String role = clubService.getMemberRole(clubId, userId);
		return ResponseEntity.ok(role);
	}
}