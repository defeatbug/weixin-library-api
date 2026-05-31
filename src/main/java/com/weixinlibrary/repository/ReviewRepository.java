package com.weixinlibrary.repository;

import com.weixinlibrary.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByBook_IdOrderByCreatedAtDesc(Long bookId, Pageable pageable);
    Page<Review> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double findAverageRatingByBookId(Long bookId);

    int countByBook_Id(Long bookId);
    int countByUser_Id(Long userId);
}
