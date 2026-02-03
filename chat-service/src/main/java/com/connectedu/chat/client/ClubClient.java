package com.connectedu.chat.client;

import com.connectedu.chat.dto.ChatClubDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "club-service")
public interface ClubClient {
    @GetMapping("/internal/chat/clubs/all")
    List<ChatClubDto> getAllClubs();
}