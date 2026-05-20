package com.weixinlibrary.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books", indexes = {
        @Index(name = "idx_book_title", columnList = "title"),
        @Index(name = "idx_book_author", columnList = "author")
})
public class Book extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    private String isbn;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private BookFileType fileType;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String publisher;

    @Column(name = "published_at")
    private String publishedAt;

    private String language;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes = 0L;

    public enum BookFileType {
        EPUB, PDF
    }
}
