package com.weixinlibrary.repository;

import com.weixinlibrary.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);
    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String author, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(b.isbn, '')) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Book> searchBooks(@Param("search") String search, Pageable pageable);
}
