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
@Table(name = "bookshelf_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "book_id"})
}, indexes = {
        @Index(name = "idx_bookshelf_user", columnList = "user_id"),
        @Index(name = "idx_bookshelf_user_order", columnList = "user_id, sort_order")
})
public class BookshelfItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
