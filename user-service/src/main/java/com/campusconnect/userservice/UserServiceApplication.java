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

	@Bean
	public CommandLineRunner createDefaultAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			String adminEmail = "admin@college.com";
			userRepository.findByEmail(adminEmail).orElseGet(() -> {
				User admin = User.builder().name("College Admin") // required field
						.email(adminEmail) // required field
						.password(passwordEncoder.encode("admin123")) // encrypted
						.department("Administration") 
						.roles(Role.ADMIN) 
						.provider("LOCAL") 
						.providerId(null) 
						.build();
				return userRepository.save(admin);
			});
		};
	}

}