package com.weixinlibrary.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reading_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "book_id"})
}, indexes = {
        @Index(name = "idx_reading_progress_user_book", columnList = "user_id, book_id")
})
public class ReadingProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "current_chapter_id")
    private String currentChapterId;

    @Column(name = "current_chapter_title")
    private String currentChapterTitle;

    @Column(name = "page_offset")
    private Integer pageOffset = 0;

    @Column(nullable = false)
    private Double percentage = 0.0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
