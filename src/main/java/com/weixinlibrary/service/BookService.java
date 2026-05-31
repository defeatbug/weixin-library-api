package com.weixinlibrary.service;

import com.weixinlibrary.entity.Book;
import com.weixinlibrary.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class BookService {

    private final BookRepository bookRepository;

    public Page<Book> findBooks(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findAll(pageable);
    }

    public Page<Book> searchBooks(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findByTitleContainingIgnoreCase(query, pageable);
    }

    public Page<Book> adminSearchBooks(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (search == null || search.isBlank()) {
            return bookRepository.findAll(pageable);
        }
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                search, search, pageable);
    }

    public Book getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found: " + id));
    }

    @Transactional
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Long id, Book updated) {
        Book book = getBook(id);
        if (updated.getTitle() != null) book.setTitle(updated.getTitle());
        if (updated.getAuthor() != null) book.setAuthor(updated.getAuthor());
        if (updated.getIsbn() != null) book.setIsbn(updated.getIsbn());
        if (updated.getCoverUrl() != null) book.setCoverUrl(updated.getCoverUrl());
        if (updated.getDescription() != null) book.setDescription(updated.getDescription());
        if (updated.getPublisher() != null) book.setPublisher(updated.getPublisher());
        if (updated.getPublishedAt() != null) book.setPublishedAt(updated.getPublishedAt());
        if (updated.getLanguage() != null) book.setLanguage(updated.getLanguage());
        if (updated.getFileUrl() != null) book.setFileUrl(updated.getFileUrl());
        if (updated.getFileType() != null) book.setFileType(updated.getFileType());
        if (updated.getFileSizeBytes() != null) book.setFileSizeBytes(updated.getFileSizeBytes());
        return bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
}
