package com.campusconnect.discussionservice.client;

import com.campusconnect.discussionservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // --- ADD ---
import org.springframework.web.bind.annotation.RequestBody; // --- ADD ---
import java.util.List; // --- ADD ---

@FeignClient(name = "user-service", path = "/internal/api/users")
public interface UserClient {

    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @PostMapping("/batch")
    List<UserDto> getUsersByIds(@RequestBody List<Long> userIds);
}