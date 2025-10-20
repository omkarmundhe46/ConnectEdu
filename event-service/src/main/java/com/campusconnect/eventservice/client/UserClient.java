package com.campusconnect.eventservice.client;

import com.campusconnect.eventservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // Import
import org.springframework.web.bind.annotation.RequestBody; // Import

import java.util.List; // Import

// Point the client to the unsecured internal endpoint
@FeignClient(name = "user-service", url = "${USER_SERVICE_URL:http://localhost:8081}/internal/api/users")
public interface UserClient {

    @GetMapping("/{id}") // Path is now relative
    UserDto getUserById(@PathVariable("id") Long id);

    // --- ADD THIS NEW METHOD ---
    @PostMapping("/batch")
    List<UserDto> getUsersByIds(@RequestBody List<Long> userIds);
}