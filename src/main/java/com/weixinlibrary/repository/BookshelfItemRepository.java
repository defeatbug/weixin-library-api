package com.weixinlibrary.repository;

import com.weixinlibrary.entity.BookshelfItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookshelfItemRepository extends JpaRepository<BookshelfItem, Long> {
    List<BookshelfItem> findByUserIdOrderBySortOrderAsc(Long userId);
    Optional<BookshelfItem> findByUserIdAndBookId(Long userId, Long bookId);
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    void deleteByUserIdAndBookId(Long userId, Long bookId);
    int countByUserId(Long userId);
}
