package com.weixinlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookPageInput {
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDir = "desc";
}
