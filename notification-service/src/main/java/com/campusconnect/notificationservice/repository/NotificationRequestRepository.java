package com.campusconnect.notificationservice.repository;

import com.campusconnect.notificationservice.entity.NotificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRequestRepository extends JpaRepository<NotificationRequest, Long> {

    boolean existsByRequestId(String requestId);

    Optional<NotificationRequest> findByRequestId(String requestId);

    boolean existsByNotificationTypeAndUserIdAndEventId(
            String notificationType,
            Long userId,
            Long eventId
    );
}