package com.eventpass.backend.config;

import com.eventpass.backend.entity.User;
import com.eventpass.backend.enums.Role;
import com.eventpass.backend.enums.UserStatus;
import com.eventpass.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    public ApplicationRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail("admin@eventpass.com")) {
                userRepository.save(User.builder()
                        .name("Admin EventPass")
                        .email("admin@eventpass.com")
                        .password(passwordEncoder.encode("EventPass@1234!"))
                        .role(Role.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .build());
                System.out.println("✅ Admin créé : admin@eventpass.com / EventPass@1234!");
            }
        };
    }
}