package com.campusconnect.clubservice.repository;

import com.campusconnect.clubservice.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    List<ClubMember> findByClubId(Long clubId);
    Optional<ClubMember> findByClubIdAndUserId(Long clubId, Long userId);
    boolean existsByClubIdAndUserId(Long clubId, Long userId);
    List<ClubMember> findByUserId(Long userId);
}