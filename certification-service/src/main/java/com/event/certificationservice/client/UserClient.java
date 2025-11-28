package com.event.certificationservice.client;

import com.event.certificationservice.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// --- THIS IS THE FIX ---
// Point to the INTERNAL, unsecured path
@FeignClient(name = "user-service", path = "/internal/api/users")
public interface UserClient {

    @GetMapping("/{id}")
    UserResponseDto getUserById(@PathVariable("id") Long id);
}