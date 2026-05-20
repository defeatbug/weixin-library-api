package com.weixinlibrary.graphql;

import com.weixinlibrary.entity.User;
import com.weixinlibrary.repository.BookshelfItemRepository;
import com.weixinlibrary.repository.ReviewRepository;
import com.weixinlibrary.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserResolver {

    private final AuthService authService;
    private final BookshelfItemRepository bookshelfItemRepository;
    private final ReviewRepository reviewRepository;

    @QueryMapping
    public User currentUser(Authentication authentication) {
        if (authentication == null) return null;
        Long userId = (Long) authentication.getDetails();
        return authService.getCurrentUser(userId);
    }

    @SchemaMapping(typeName = "User", field = "bookshelfCount")
    public int bookshelfCount(User user) {
        return bookshelfItemRepository.countByUserId(user.getId());
    }

    @SchemaMapping(typeName = "User", field = "reviewCount")
    public int reviewCount(User user) {
        return reviewRepository.countByUserId(user.getId());
    }
}
