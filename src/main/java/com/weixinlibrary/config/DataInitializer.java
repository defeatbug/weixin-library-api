package com.weixinlibrary.config;

import com.weixinlibrary.entity.User;
import com.weixinlibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@weixin.library")) {
            User admin = new User();
            admin.setEmail("admin@weixin.library");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setDisplayName("Admin");
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            log.info("Default admin user created: admin@weixin.library / admin123");
        }

        for (int i = 1; i <= 10; i++) {
            String email = "user" + i + "@weixin.library";
            if (userRepository.existsByEmail(email)) {
                continue;
            }
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setDisplayName("User" + i);
            user.setRole(User.Role.USER);
            userRepository.save(user);
            log.info("Test user created: {} / 123456", email);
        }
    }
}
