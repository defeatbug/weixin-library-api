package com.weixinlibrary.graphql;

import com.weixinlibrary.dto.PageResponse;
import com.weixinlibrary.entity.Review;
import com.weixinlibrary.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ReviewResolver {

    private final ReviewService reviewService;

    @QueryMapping
    public PageResponse<Review> reviewsByBook(@Argument Long bookId,
                                              @Argument int page, @Argument int size) {
        Page<Review> result = reviewService.getReviewsByBook(bookId, page, size);
        return PageResponse.from(result, result.getContent());
    }

    @QueryMapping
    public PageResponse<Review> myReviews(@Argument int page, @Argument int size,
                                          Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        Page<Review> result = reviewService.getMyReviews(userId, page, size);
        return PageResponse.from(result, result.getContent());
    }

    @MutationMapping
    public Review createReview(@Argument Long bookId, @Argument int rating,
                               @Argument String content, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        return reviewService.createReview(userId, bookId, rating, content);
    }

    @MutationMapping
    public Review updateReview(@Argument Long id, @Argument Integer rating,
                                 @Argument String content) {
        return reviewService.updateReview(id, rating, content);
    }

    @MutationMapping
    public Boolean deleteReview(@Argument Long id) {
        reviewService.deleteReview(id);
        return true;
    }
}
