package com.weixinlibrary.graphql;

import com.weixinlibrary.dto.CreateBookInput;
import com.weixinlibrary.dto.PageResponse;
import com.weixinlibrary.entity.Book;
import com.weixinlibrary.service.BookService;
import com.weixinlibrary.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class BookResolver {

    private final BookService bookService;
    private final ReviewService reviewService;

    @QueryMapping
    public PageResponse<Book> books(@Argument int page, @Argument int size,
                                    @Argument String sortBy, @Argument String sortDir) {
        Page<Book> result = bookService.findBooks(page, size, sortBy, sortDir);
        return PageResponse.from(result, result.getContent());
    }

    @QueryMapping
    public Book book(@Argument Long id) {
        return bookService.getBook(id);
    }

    @QueryMapping
    public PageResponse<Book> searchBooks(@Argument String query,
                                          @Argument int page, @Argument int size) {
        Page<Book> result = bookService.searchBooks(query, page, size);
        return PageResponse.from(result, result.getContent());
    }

    @MutationMapping
    public Book createBook(@Argument("input") CreateBookInput input) {
        Book book = new Book();
        book.setTitle(input.getTitle());
        book.setAuthor(input.getAuthor());
        book.setIsbn(input.getIsbn());
        book.setCoverUrl(input.getCoverUrl());
        book.setFileUrl(input.getFileUrl());
        book.setFileType(Book.BookFileType.valueOf(input.getFileType()));
        book.setDescription(input.getDescription());
        book.setPublisher(input.getPublisher());
        book.setPublishedAt(input.getPublishedAt());
        book.setLanguage(input.getLanguage());
        book.setFileSizeBytes(input.getFileSizeBytes());
        return bookService.createBook(book);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Book updateBook(@Argument Long id, @Argument("input") Book book) {
        return bookService.updateBook(id, book);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteBook(@Argument Long id) {
        bookService.deleteBook(id);
        return true;
    }

    @SchemaMapping(typeName = "Book", field = "averageRating")
    public Double averageRating(Book book) {
        return reviewService.getAverageRating(book.getId());
    }

    @SchemaMapping(typeName = "Book", field = "reviewCount")
    public int reviewCount(Book book) {
        return reviewService.getReviewCount(book.getId());
    }
}
