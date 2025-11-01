package com.campusconnect.discussionservice.repository;
import com.campusconnect.discussionservice.entity.DiscussionMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface DiscussionMessageRepository extends JpaRepository<DiscussionMessage, Long> {
    List<DiscussionMessage> findByEventIdOrderBySentAtAsc(Long eventId);

    @Transactional
    void deleteByEventIdIn(List<Long> eventIds);
}