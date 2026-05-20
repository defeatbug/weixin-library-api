package com.weixinlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> items;
    private long total;
    private int page;
    private int size;

    public static <T> PageResponse<T> from(Page<?> page, List<T> items) {
        return new PageResponse<>(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }
}
