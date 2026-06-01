package com.weixinlibrary.repository;

import com.weixinlibrary.entity.ReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {
    Optional<ReadingProgress> findByUser_IdAndBook_Id(Long userId, Long bookId);

    @Query("SELECT COUNT(rp) FROM ReadingProgress rp WHERE rp.user.id = :userId " +
           "AND rp.percentage > 0 AND rp.percentage < 100")
    int countReadingByUserId(@Param("userId") Long userId);
}
