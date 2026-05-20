package com.weixinlibrary.graphql;

import com.weixinlibrary.entity.BookshelfItem;
import com.weixinlibrary.service.BookshelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookshelfResolver {

    private final BookshelfService bookshelfService;

    @QueryMapping
    public List<BookshelfItem> myBookshelf(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        return bookshelfService.getBookshelf(userId);
    }

    @MutationMapping
    public BookshelfItem addToBookshelf(@Argument Long bookId, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        return bookshelfService.addToBookshelf(userId, bookId);
    }

    @MutationMapping
    public Boolean removeFromBookshelf(@Argument Long bookId, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        bookshelfService.removeFromBookshelf(userId, bookId);
        return true;
    }
}
