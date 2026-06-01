package com.weixinlibrary.service;

import com.weixinlibrary.entity.User;
import com.weixinlibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Page<User> findUsers(int page, int size, String search) {
        var pageable = PageRequest.of(page, size);
        if (search != null && !search.isBlank()) {
            return userRepository.searchUsers(search.trim(), pageable);
        }
        return userRepository.findAll(pageable);
    }
}
