package com.weixinlibrary.service;

import com.weixinlibrary.entity.Book;
import com.weixinlibrary.entity.BookshelfItem;
import com.weixinlibrary.entity.User;
import com.weixinlibrary.repository.BookRepository;
import com.weixinlibrary.repository.BookshelfItemRepository;
import com.weixinlibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class BookshelfService {

    private final BookshelfItemRepository bookshelfItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public List<BookshelfItem> getBookshelf(Long userId) {
        return bookshelfItemRepository.findByUser_IdOrderBySortOrderAsc(userId);
    }

    @Transactional
    public BookshelfItem addToBookshelf(Long userId, Long bookId) {
        if (bookshelfItemRepository.existsByUser_IdAndBook_Id(userId, bookId)) {
            throw new RuntimeException("Book already in bookshelf");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        int count = bookshelfItemRepository.countByUser_Id(userId);

        BookshelfItem item = new BookshelfItem();
        item.setUser(user);
        item.setBook(book);
        item.setAddedAt(LocalDateTime.now());
        item.setSortOrder(count);
        return bookshelfItemRepository.save(item);
    }

    @Transactional
    public void removeFromBookshelf(Long userId, Long bookId) {
        bookshelfItemRepository.deleteByUser_IdAndBook_Id(userId, bookId);
    }

    @Transactional
    public List<BookshelfItem> reorderBookshelf(Long userId, List<Long> bookIds) {
        List<BookshelfItem> items = bookshelfItemRepository.findByUser_IdOrderBySortOrderAsc(userId);
        for (int i = 0; i < bookIds.size(); i++) {
            Long bookId = bookIds.get(i);
            int sortOrder = i;
            items.stream()
                    .filter(item -> item.getBook().getId().equals(bookId))
                    .findFirst()
                    .ifPresent(item -> item.setSortOrder(sortOrder));
        }
        return bookshelfItemRepository.saveAll(items);
    }
}
