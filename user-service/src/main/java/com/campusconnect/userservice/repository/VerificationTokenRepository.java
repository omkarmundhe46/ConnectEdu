package com.campusconnect.userservice.repository;

import com.campusconnect.userservice.entity.User;
import com.campusconnect.userservice.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByOtp(String otp);
    Optional<VerificationToken> findByUser(User user);
}