package com.campusconnect.userservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.campusconnect.userservice.entity.Role;
import com.campusconnect.userservice.entity.User;
import com.campusconnect.userservice.repository.UserRepository;

@SpringBootApplication
@EnableFeignClients
@EnableAsync
public class UserServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}