package com.weixinlibrary.service;

import com.weixinlibrary.dto.AuthPayload;
import com.weixinlibrary.dto.LoginInput;
import com.weixinlibrary.dto.RegisterInput;
import com.weixinlibrary.entity.User;
import com.weixinlibrary.repository.UserRepository;
import com.weixinlibrary.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public AuthPayload register(RegisterInput input) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setDisplayName(input.getDisplayName());
        user.setRole(User.Role.USER);
        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());
        return new AuthPayload(user, token);
    }

    @Transactional
    public AuthPayload login(LoginInput input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(input.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Kick existing session
        String sessionKey = "session:" + user.getId();
        String oldToken = redisTemplate.opsForValue().get(sessionKey);
        if (oldToken != null) {
            redisTemplate.delete("token:" + oldToken);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());
        redisTemplate.opsForValue().set(sessionKey, token);

        return new AuthPayload(user, token);
    }

    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
