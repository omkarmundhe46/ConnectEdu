package com.campusconnect.clubservice.service;

import com.campusconnect.clubservice.entity.Banner;
import com.campusconnect.clubservice.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    public Banner createBanner(String title, String imageUrl, String linkUrl, Long clubId) {
        Banner banner = Banner.builder()
                .title(title)
                .imageUrl(imageUrl)
                .linkUrl(linkUrl)
                .clubId(clubId) // Can be null
                .active(true)
                .build();
        return bannerRepository.save(banner);
    }

    public List<Banner> getAllActiveBanners() {
        return bannerRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    public List<Banner> getBannersForClub(Long clubId) {
        return bannerRepository.findByClubIdOrderByCreatedAtDesc(clubId);
    }

    public List<Banner> getCollegeBanners() {
        return bannerRepository.findByClubIdIsNullOrderByCreatedAtDesc();
    }

    public void deleteBanner(Long bannerId) {
        if (bannerRepository.existsById(bannerId)) {
            bannerRepository.deleteById(bannerId);
        }
    }
}