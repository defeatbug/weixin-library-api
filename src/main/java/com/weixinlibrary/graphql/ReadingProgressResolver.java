package com.weixinlibrary.graphql;

import com.weixinlibrary.entity.ReadingProgress;
import com.weixinlibrary.service.ReadingProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ReadingProgressResolver {

    private final ReadingProgressService readingProgressService;

    @QueryMapping
    public ReadingProgress readingProgress(@Argument Long bookId, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        return readingProgressService.getProgress(userId, bookId);
    }

    @MutationMapping
    public ReadingProgress saveReadingProgress(@Argument Long bookId,
                                                @Argument String chapterId,
                                                @Argument String chapterTitle,
                                                @Argument Integer pageOffset,
                                                @Argument Double percentage,
                                                Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        return readingProgressService.saveProgress(userId, bookId, chapterId,
                chapterTitle, pageOffset, percentage);
    }
}
