package com.campusconnect.clubservice.client;

import com.campusconnect.clubservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.campusconnect.clubservice.dto.UpdateUserRoleRequest; // Import the new DTO

@FeignClient(name = "user-service", url = "${USER_SERVICE_URL:http://localhost:8081}")
public interface UserClient {
    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    // ADD THIS NEW METHOD
    @PutMapping("/api/users/{id}/role")
    void updateUserRole(@PathVariable("id") Long id, @RequestBody UpdateUserRoleRequest request);
}