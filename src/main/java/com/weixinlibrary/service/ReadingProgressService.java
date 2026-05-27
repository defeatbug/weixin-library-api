package com.weixinlibrary.service;

import com.weixinlibrary.entity.Book;
import com.weixinlibrary.entity.ReadingProgress;
import com.weixinlibrary.entity.User;
import com.weixinlibrary.repository.BookRepository;
import com.weixinlibrary.repository.ReadingProgressRepository;
import com.weixinlibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ReadingProgressService {

    private final ReadingProgressRepository readingProgressRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public ReadingProgress getProgress(Long userId, Long bookId) {
        return readingProgressRepository.findByUserIdAndBookId(userId, bookId).orElse(null);
    }

    @Transactional
    public ReadingProgress saveProgress(Long userId, Long bookId, String chapterId,
                                         String chapterTitle, Integer pageOffset, Double percentage) {
        ReadingProgress progress = readingProgressRepository
                .findByUserIdAndBookId(userId, bookId)
                .orElseGet(() -> {
                    ReadingProgress newProgress = new ReadingProgress();
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    Book book = bookRepository.findById(bookId)
                            .orElseThrow(() -> new RuntimeException("Book not found"));
                    newProgress.setUser(user);
                    newProgress.setBook(book);
                    return newProgress;
                });

        if (chapterId != null) progress.setCurrentChapterId(chapterId);
        if (chapterTitle != null) progress.setCurrentChapterTitle(chapterTitle);
        if (pageOffset != null) progress.setPageOffset(pageOffset);
        if (percentage != null) progress.setPercentage(percentage);
        progress.setUpdatedAt(LocalDateTime.now());

        return readingProgressRepository.save(progress);
    }
}
