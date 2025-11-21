package com.campusconnect.clubservice.repository;

import com.campusconnect.clubservice.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    // Fetch all active banners, ordered by newest first
    List<Banner> findByActiveTrueOrderByCreatedAtDesc();

    // Fetch banners for a specific club
    List<Banner> findByClubIdOrderByCreatedAtDesc(Long clubId);

    // Fetch banners created by College Admin (clubId is null)
    List<Banner> findByClubIdIsNullOrderByCreatedAtDesc();
}