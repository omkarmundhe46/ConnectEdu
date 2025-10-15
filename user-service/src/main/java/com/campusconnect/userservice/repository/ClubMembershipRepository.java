package com.campusconnect.userservice.repository;

import com.campusconnect.userservice.entity.ClubMembership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {
    boolean existsByUserIdAndClubId(Long userId, Long clubId);
}