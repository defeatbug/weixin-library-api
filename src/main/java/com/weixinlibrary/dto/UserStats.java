package com.weixinlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserStats {
    private int bookshelfCount;
    private int reviewCount;
    private int booksReadingCount;
}
