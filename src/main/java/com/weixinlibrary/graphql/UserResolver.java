package com.weixinlibrary.graphql;

import com.weixinlibrary.dto.PageResponse;
import com.weixinlibrary.dto.UserStats;
import com.weixinlibrary.entity.User;
import com.weixinlibrary.repository.BookshelfItemRepository;
import com.weixinlibrary.repository.ReviewRepository;
import com.weixinlibrary.service.AuthService;
import com.weixinlibrary.service.StatsService;
import com.weixinlibrary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserResolver {

    private final AuthService authService;
    private final BookshelfItemRepository bookshelfItemRepository;
    private final ReviewRepository reviewRepository;
    private final StatsService statsService;
    private final UserService userService;

    @QueryMapping
    public User currentUser(Authentication authentication) {
        if (authentication == null) return null;
        Long userId = (Long) authentication.getDetails();
        return authService.getCurrentUser(userId);
    }

    @QueryMapping
    public UserStats myStats(Authentication authentication) {
        if (authentication == null) return null;
        Long userId = (Long) authentication.getDetails();
        return statsService.getMyStats(userId);
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<User> adminUsers(@Argument int page, @Argument int size,
                                         @Argument String search) {
        Page<User> result = userService.findUsers(page, size, search);
        return PageResponse.from(result, result.getContent());
    }

    @SchemaMapping(typeName = "User", field = "role")
    public String role(User user) {
        return user.getRole().name();
    }

    @SchemaMapping(typeName = "User", field = "bookshelfCount")
    public int bookshelfCount(User user) {
        return bookshelfItemRepository.countByUser_Id(user.getId());
    }

    @SchemaMapping(typeName = "User", field = "reviewCount")
    public int reviewCount(User user) {
        return reviewRepository.countByUser_Id(user.getId());
    }
}
