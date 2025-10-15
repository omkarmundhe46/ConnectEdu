package com.event.certificationservice.client;

import com.event.certificationservice.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${USER_SERVICE_URL}")
public interface UserClient {
    @GetMapping("/api/users/{id}")
    UserResponseDto getUserById(@PathVariable Long id);
}