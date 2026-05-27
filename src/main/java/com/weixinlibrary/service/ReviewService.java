package com.weixinlibrary.service;

import com.weixinlibrary.entity.Book;
import com.weixinlibrary.entity.Review;
import com.weixinlibrary.entity.User;
import com.weixinlibrary.repository.BookRepository;
import com.weixinlibrary.repository.ReviewRepository;
import com.weixinlibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public Page<Review> getReviewsByBook(Long bookId, int page, int size) {
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId, PageRequest.of(page, size));
    }

    public Page<Review> getMyReviews(Long userId, int page, int size) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    public Double getAverageRating(Long bookId) {
        return reviewRepository.findAverageRatingByBookId(bookId);
    }

    public int getReviewCount(Long bookId) {
        return reviewRepository.countByBookId(bookId);
    }

    @Transactional
    public Review createReview(Long userId, Long bookId, Integer rating, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(rating);
        review.setContent(content);
        return reviewRepository.save(review);
    }

    @Transactional
    public Review updateReview(Long reviewId, Integer rating, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        if (rating != null) review.setRating(rating);
        if (content != null) review.setContent(content);
        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
