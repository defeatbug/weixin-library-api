package com.weixinlibrary.controller;

import com.weixinlibrary.service.BookContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files/books")
@RequiredArgsConstructor
public class BookFileController {

    private final BookContentService bookContentService;

    @GetMapping(value = "/{filename:.+}/text", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public ResponseEntity<String> getText(@PathVariable String filename) {
        try {
            String text = bookContentService.extractText(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                    .body(text);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("内容读取失败: " + e.getMessage());
        }
    }

    @GetMapping("/{filename:.+}/toc")
    public ResponseEntity<Map<String, Object>> getToc(@PathVariable String filename) {
        try {
            List<Map<String, String>> chapters = bookContentService.extractToc(filename);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("chapters", chapters);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "目录读取失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{filename:.+}/cover")
    public ResponseEntity<byte[]> getCover(@PathVariable String filename) {
        try {
            var cover = bookContentService.extractCover(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .contentType(MediaType.parseMediaType(cover.contentType()))
                    .body(cover.data());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
