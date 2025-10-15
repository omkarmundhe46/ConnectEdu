package com.campusconnect.discussionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DiscussionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscussionServiceApplication.class, args);
    }
}