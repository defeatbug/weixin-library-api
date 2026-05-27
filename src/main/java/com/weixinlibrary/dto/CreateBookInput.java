package com.weixinlibrary.dto;

import lombok.Data;

@Data
public class CreateBookInput {
    private String title;
    private String author;
    private String isbn;
    private String coverUrl;
    private String fileUrl;
    private String fileType;
    private String description;
    private String publisher;
    private String publishedAt;
    private String language;
    private Long fileSizeBytes;
}
