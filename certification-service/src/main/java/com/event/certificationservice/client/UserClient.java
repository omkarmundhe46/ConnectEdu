package com.event.certificationservice.client;

import com.event.certificationservice.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Update the URL to include the /internal prefix
@FeignClient(name = "user-service", url = "${USER_SERVICE_URL:http://localhost:8081}/internal/api/users")
public interface UserClient {

    // The path is now relative to the new base URL
    @GetMapping("/{id}")
    UserResponseDto getUserById(@PathVariable("id") Long id);
}