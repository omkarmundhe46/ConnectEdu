package com.campusconnect.clubservice.client;

import com.campusconnect.clubservice.dto.UpdateUserRoleRequest;
import com.campusconnect.clubservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

// 1. CHANGE: Point to the ROOT of the user-service
@FeignClient(name = "user-service", url = "${USER_SERVICE_URL:http://localhost:8081}")
public interface UserClient {

    // 2. CHANGE: Add the FULL internal path
    @GetMapping("/internal/api/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    // 3. CHANGE: Add the FULL *secured* path
    @PutMapping("/api/users/{id}/role")
    void updateUserRole(@PathVariable("id") Long id, @RequestBody UpdateUserRoleRequest request);

    // 4. CHANGE: Add the FULL internal path
    @GetMapping("/internal/api/users/by-email/{email}")
    UserDto getUserByEmail(@PathVariable("email") String email);
}