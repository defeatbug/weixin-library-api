package com.weixinlibrary.repository;

import com.weixinlibrary.entity.ReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {
    Optional<ReadingProgress> findByUserIdAndBookId(Long userId, Long bookId);
}
