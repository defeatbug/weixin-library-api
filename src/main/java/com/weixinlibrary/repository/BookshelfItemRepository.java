package com.weixinlibrary.repository;

import com.weixinlibrary.entity.BookshelfItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookshelfItemRepository extends JpaRepository<BookshelfItem, Long> {
    List<BookshelfItem> findByUser_IdOrderBySortOrderAsc(Long userId);
    Optional<BookshelfItem> findByUser_IdAndBook_Id(Long userId, Long bookId);
    boolean existsByUser_IdAndBook_Id(Long userId, Long bookId);
    void deleteByUser_IdAndBook_Id(Long userId, Long bookId);
    int countByUser_Id(Long userId);
}
