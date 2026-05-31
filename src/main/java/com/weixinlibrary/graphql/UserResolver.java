package com.weixinlibrary.graphql;

import com.weixinlibrary.dto.PageResponse;
import com.weixinlibrary.entity.User;
import com.weixinlibrary.repository.BookshelfItemRepository;
import com.weixinlibrary.repository.ReviewRepository;
import com.weixinlibrary.repository.UserRepository;
import com.weixinlibrary.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserResolver {

    private final AuthService authService;
    private final BookshelfItemRepository bookshelfItemRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @QueryMapping
    public User currentUser(Authentication authentication) {
        if (authentication == null) return null;
        Long userId = (Long) authentication.getDetails();
        return authService.getCurrentUser(userId);
    }

    @QueryMapping
    public Map<String, Integer> myStats(Authentication authentication) {
        if (authentication == null) return null;
        Long userId = (Long) authentication.getDetails();
        int bookshelfCount = bookshelfItemRepository.countByUser_Id(userId);
        int reviewCount = reviewRepository.countByUser_Id(userId);
        return Map.of(
            "bookshelfCount", bookshelfCount,
            "reviewCount", reviewCount,
            "booksReadingCount", bookshelfCount
        );
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<User> adminUsers(@Argument int page, @Argument int size,
                                         @Argument String search) {
        Page<User> result;
        if (search == null || search.isBlank()) {
            result = userRepository.findAll(PageRequest.of(page, size));
        } else {
            result = userRepository
                    .findByEmailContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
                            search, search, PageRequest.of(page, size));
        }
        return PageResponse.from(result, result.getContent());
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
