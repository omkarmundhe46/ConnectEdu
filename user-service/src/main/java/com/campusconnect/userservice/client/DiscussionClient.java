//package com.campusconnect.userservice.client;
//
//import com.campusconnect.userservice.dto.DiscussionRequestDto;
//import com.campusconnect.userservice.dto.DiscussionResponseDto;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.*;
//
//import jakarta.validation.Valid;
//import java.util.List;
//
//@FeignClient(
//    name = "discussion-service",
//    url = "${DISCUSSION_SERVICE_URL:http://localhost:8084}/api/discussions"
//)
//public interface DiscussionClient {
//
//    // Create discussion under a club event
//    @PostMapping("/event/{eventId}")
//    DiscussionResponseDto createDiscussion(@PathVariable("eventId") Long eventId,
//                                           @Valid @RequestBody DiscussionRequestDto request);
//
//    // Get discussions of an event
//    @GetMapping("/event/{eventId}")
//    List<DiscussionResponseDto> getDiscussionsByEvent(@PathVariable("eventId") Long eventId);
//
//    // Update discussion
//    @PutMapping("/{discussionId}")
//    DiscussionResponseDto updateDiscussion(@PathVariable("discussionId") Long discussionId,
//                                           @Valid @RequestBody DiscussionRequestDto request);
//
//    // Delete discussion
//    @DeleteMapping("/{discussionId}")
//    void deleteDiscussion(@PathVariable("discussionId") Long discussionId);
//}
