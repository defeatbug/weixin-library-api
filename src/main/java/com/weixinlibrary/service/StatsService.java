package com.weixinlibrary.service;

import com.weixinlibrary.dto.UserStats;
import com.weixinlibrary.repository.BookshelfItemRepository;
import com.weixinlibrary.repository.ReadingProgressRepository;
import com.weixinlibrary.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final BookshelfItemRepository bookshelfItemRepository;
    private final ReviewRepository reviewRepository;
    private final ReadingProgressRepository readingProgressRepository;

    public UserStats getMyStats(Long userId) {
        int bookshelfCount = bookshelfItemRepository.countByUser_Id(userId);
        int reviewCount = reviewRepository.countByUser_Id(userId);
        int booksReadingCount = readingProgressRepository.countReadingByUserId(userId);
        return new UserStats(bookshelfCount, reviewCount, booksReadingCount);
    }
}
