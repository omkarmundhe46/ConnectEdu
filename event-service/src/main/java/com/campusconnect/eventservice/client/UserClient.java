package com.campusconnect.eventservice.client;

import com.campusconnect.eventservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // Import
import org.springframework.web.bind.annotation.RequestBody; // Import

import java.util.List; // Import

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

//    @PostMapping("/batch")
//    List<UserDto> getUsersByIds(@RequestBody List<Long> userIds);

    @PostMapping("/internal/api/users/batch")
    List<UserDto> getUsersByIds(@RequestBody List<Long> userIds);


}