package com.campusconnect.clubservice.client;

import com.campusconnect.clubservice.dto.UpdateUserRoleRequest;
import com.campusconnect.clubservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Point to internal endpoints
@FeignClient(name = "user-service", url = "${USER_SERVICE_URL:http://localhost:8081}/internal/api/users")
public interface UserClient {
    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @PutMapping("/{id}/role")
    void updateUserRole(@PathVariable("id") Long id, @RequestBody UpdateUserRoleRequest request);

    // --- ADD: Get User by Email ---
    @GetMapping("/by-email/{email}")
    UserDto getUserByEmail(@PathVariable("email") String email);
}