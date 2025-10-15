package com.campusconnect.userservice.controller;

import com.campusconnect.userservice.client.ClubClient;
import com.campusconnect.userservice.client.EventClient;
import com.campusconnect.userservice.client.DiscussionClient;
import com.campusconnect.userservice.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/club-admin/clubs")
@PreAuthorize("hasRole('CLUB_ADMIN')")
@RequiredArgsConstructor
public class ClubAdminController {
	

    private final ClubClient clubClient;
    private final EventClient eventClient;
    private final DiscussionClient discussionClient;

    // --- Utility to check ownership ---
    private void validateClubOwnership(Long clubId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = Long.parseLong(auth.getName()); // assuming username = userId

        ClubResponseDto club = clubClient.getClubById(clubId);
        if (!club.getAdminId().equals(currentUserId)) {
            throw new SecurityException("Access denied: You are not the admin of this club");
        }
    }

    // ===============================
    // CLUB OPERATIONS
    // ===============================

    @PutMapping("/{clubId}")
    public ClubResponseDto updateClub(@PathVariable Long clubId,
                                      @RequestBody ClubResponseDto request) {
        validateClubOwnership(clubId);
        return clubClient.updateClub(clubId, request.toRequestDto());
    }

    @DeleteMapping("/{clubId}")
    public void deleteClub(@PathVariable Long clubId) {
        validateClubOwnership(clubId);
        clubClient.deleteClub(clubId);
    }

    // ===============================
    // CLUB MEMBERS
    // ===============================

    @PostMapping("/{clubId}/members")
    public ClubMemberResponseDto addMember(@PathVariable Long clubId,
                                           @RequestBody ClubMemberRequestDto request) {
        validateClubOwnership(clubId);
        return clubClient.addMember(clubId, request);
    }

    @GetMapping("/{clubId}/members")
    public List<ClubMemberResponseDto> getMembers(@PathVariable Long clubId) {
        validateClubOwnership(clubId);
        return clubClient.getMembers(clubId);
    }

    @DeleteMapping("/{clubId}/members/{userId}")
    public void removeMember(@PathVariable Long clubId, @PathVariable Long userId) {
        validateClubOwnership(clubId);
        clubClient.removeMember(clubId, userId);
    }

    // ===============================
    // EVENTS (own club only)
    // ===============================

    @PostMapping("/{clubId}/events")
    public EventResponseDto createEvent(@PathVariable Long clubId,
                                        @RequestBody EventRequestDto request) {
        validateClubOwnership(clubId);
        return eventClient.createEvent(clubId, request);
    }

    @GetMapping("/{clubId}/events")
    public List<EventResponseDto> getClubEvents(@PathVariable Long clubId) {
        validateClubOwnership(clubId);
        return eventClient.getEventsByClub(clubId);
    }

    @PutMapping("/{clubId}/events/{eventId}")
    public EventResponseDto updateEvent(@PathVariable Long clubId,
                                        @PathVariable Long eventId,
                                        @RequestBody EventRequestDto request) {
        validateClubOwnership(clubId);
        return eventClient.updateEvent(eventId, request);
    }

    @DeleteMapping("/{clubId}/events/{eventId}")
    public void deleteEvent(@PathVariable Long clubId, @PathVariable Long eventId) {
        validateClubOwnership(clubId);
        eventClient.deleteEvent(eventId);
    }

    // ===============================
    // DISCUSSIONS (within events of own club)
    // ===============================

    @PostMapping("/{clubId}/events/{eventId}/discussions")
    public DiscussionResponseDto createDiscussion(@PathVariable Long clubId,
                                                  @PathVariable Long eventId,
                                                  @RequestBody DiscussionRequestDto request) {
        validateClubOwnership(clubId);
        return discussionClient.createDiscussion(eventId, request);
    }

    @GetMapping("/{clubId}/events/{eventId}/discussions")
    public List<DiscussionResponseDto> getDiscussions(@PathVariable Long clubId,
                                                      @PathVariable Long eventId) {
        validateClubOwnership(clubId);
        return discussionClient.getDiscussionsByEvent(eventId);
    }

    @PutMapping("/{clubId}/discussions/{discussionId}")
    public DiscussionResponseDto updateDiscussion(@PathVariable Long clubId,
                                                  @PathVariable Long discussionId,
                                                  @RequestBody DiscussionRequestDto request) {
        validateClubOwnership(clubId);
        return discussionClient.updateDiscussion(discussionId, request);
    }

    @DeleteMapping("/{clubId}/discussions/{discussionId}")
    public void deleteDiscussion(@PathVariable Long clubId,
                                 @PathVariable Long discussionId) {
        validateClubOwnership(clubId);
        discussionClient.deleteDiscussion(discussionId);
    }
}
