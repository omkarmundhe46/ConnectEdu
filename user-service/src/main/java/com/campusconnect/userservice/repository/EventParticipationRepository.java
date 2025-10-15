package com.campusconnect.userservice.repository;

import com.campusconnect.userservice.entity.EventParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventParticipationRepository extends JpaRepository<EventParticipation, Long> {
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}