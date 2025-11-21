package com.campusconnect.clubservice.controller;

import com.campusconnect.clubservice.entity.Banner;
import com.campusconnect.clubservice.repository.ClubMemberRepository;
import com.campusconnect.clubservice.repository.ClubRepository;
import com.campusconnect.clubservice.service.BannerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubRepository clubRepository;

    // Public endpoint for the Home Screen carousel
    @GetMapping("/active")
    public ResponseEntity<List<Banner>> getActiveBanners() {
        return ResponseEntity.ok(bannerService.getAllActiveBanners());
    }

    // Endpoint for Club Admins/Members to see *their* banners
    @GetMapping("/club/{clubId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLUB_ADMIN', 'ROLE_CLUB_MEMBER')")
    public ResponseEntity<List<Banner>> getClubBanners(@PathVariable Long clubId) {
        return ResponseEntity.ok(bannerService.getBannersForClub(clubId));
    }

    // Endpoint for College Admins to see their banners
    @GetMapping("/college")
    @PreAuthorize("hasAuthority('ROLE_COLLEGE_ADMIN')")
    public ResponseEntity<List<Banner>> getCollegeBanners() {
        return ResponseEntity.ok(bannerService.getCollegeBanners());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_COLLEGE_ADMIN', 'ROLE_CLUB_ADMIN', 'ROLE_CLUB_MEMBER')")
    public ResponseEntity<Banner> createBanner(@Valid @RequestBody BannerRequestDto request) {
        Long userId = getAuthenticatedUserId();
        String userRole = getAuthenticatedUserRole();

        // --- SECURITY CHECK ---
        if (request.getClubId() != null) {
            // If it's a club banner, verify the user belongs to that club
            if (!isMemberOrAdminOfClub(request.getClubId(), userId)) {
                throw new AccessDeniedException("You are not authorized to post banners for this club.");
            }
        } else {
            // If clubId is null (College Banner), only College Admin can post
            if (!"COLLEGE_ADMIN".equals(userRole)) {
                throw new AccessDeniedException("Only College Admins can post college-wide banners.");
            }
        }
        Banner banner = bannerService.createBanner(
                request.getTitle(),
                request.getImageUrl(),
                request.getLinkUrl(),
                request.getClubId()
        );
        return ResponseEntity.ok(banner);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_COLLEGE_ADMIN', 'ROLE_CLUB_ADMIN', 'ROLE_CLUB_MEMBER')")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isMemberOrAdminOfClub(Long clubId, Long userId) {
        // 1. Check if they are a member
        if (clubMemberRepository.existsByClubIdAndUserId(clubId, userId)) {
            return true;
        }
        // 2. Check if they are the admin (owner) of the club
        return clubRepository.findById(clubId)
                .map(club -> club.getAdminId().equals(userId))
                .orElse(false);
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Object userIdObj = jwt.getClaim("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        throw new IllegalStateException("User ID not found in token.");
    }

    private String getAuthenticatedUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // This logic depends on how you store roles in the token.
        // Usually it's in a "roles" claim list.
        // For simplicity, let's assume the first role is primary.
        Jwt jwt = (Jwt) authentication.getPrincipal();
        List<String> roles = jwt.getClaim("roles");
        if (roles != null && !roles.isEmpty()) {
            return roles.get(0).replace("ROLE_", "");
        }
        return "USER";
    }
}

@Data
class BannerRequestDto {
    @NotBlank
    private String title;
    @NotBlank
    private String imageUrl;
    private String linkUrl;
    private Long clubId; // Nullable
}