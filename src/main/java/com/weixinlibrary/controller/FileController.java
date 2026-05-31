package com.weixinlibrary.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final Path uploadDir;

    public FileController(
            @Value("${app.file.upload-dir:./uploads/books}") String uploadDirStr) {
        this.uploadDir = Paths.get(uploadDirStr).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "File is empty"
            ));
        }

        try {
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID() + extension;
            Path targetPath = uploadDir.resolve(storedName);
            Files.copy(file.getInputStream(), targetPath);

            String fileUrl = "/api/files/" + storedName;

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "fileUrl", fileUrl,
                    "fileName", storedName,
                    "fileSize", file.getSize(),
                    "originalName", originalName
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Upload failed: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<org.springframework.core.io.Resource> getFile(
            @PathVariable String fileName) {
        try {
            Path filePath = uploadDir.resolve(fileName).normalize();
            if (!filePath.startsWith(uploadDir)) {
                return ResponseEntity.badRequest().build();
            }
            var resource = new org.springframework.core.io.UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header("Content-Type", "application/octet-stream")
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{fileName}/toc")
    public ResponseEntity<Map<String, Object>> getFileToc(@PathVariable String fileName) {
        try {
            Path filePath = uploadDir.resolve(fileName).normalize();
            if (!filePath.startsWith(uploadDir)) {
                return ResponseEntity.badRequest().build();
            }
            if (!fileName.toLowerCase().endsWith(".epub")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only EPUB supported"));
            }
            Map<String, byte[]> files = readZipFiles(filePath);
            java.util.List<Map<String, Object>> chapters = extractEpubToc(files);

            if (chapters.isEmpty()) {
                chapters = extractSpineFallback(files);
            }

            return ResponseEntity.ok(Map.of("chapters", chapters));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to extract TOC: " + e.getMessage()));
        }
    }

    @GetMapping("/{fileName}/text")
    public ResponseEntity<String> getFileText(@PathVariable String fileName) {
        try {
            Path filePath = uploadDir.resolve(fileName).normalize();
            if (!filePath.startsWith(uploadDir)) {
                return ResponseEntity.badRequest().build();
            }

            String name = fileName.toLowerCase();
            if (name.endsWith(".txt")) {
                String content = Files.readString(filePath, StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                        .header("Content-Type", "text/plain; charset=utf-8")
                        .body(content);
            } else if (name.endsWith(".epub")) {
                String text = extractEpubText(filePath);
                if (text == null || text.isBlank()) {
                    text = "（无法提取 EPUB 内容）";
                }
                return ResponseEntity.ok()
                        .header("Content-Type", "text/plain; charset=utf-8")
                        .body(text);
            } else {
                return ResponseEntity.badRequest().body("Unsupported format: " + fileName);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to extract text: " + e.getMessage());
        }
    }

    private Map<String, byte[]> readZipFiles(Path zipPath) throws IOException {
        Map<String, byte[]> files = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = zis.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                    }
                    files.put(entry.getName(), baos.toByteArray());
                }
                zis.closeEntry();
            }
        }
        return files;
    }

    private String extractEpubText(Path epubPath) throws Exception {
        Map<String, byte[]> files = readZipFiles(epubPath);

        // Parse container.xml to find OPF path
        byte[] containerBytes = files.get("META-INF/container.xml");
        if (containerBytes == null) return null;

        String opfPath = parseOpfPathFromContainer(new String(containerBytes, StandardCharsets.UTF_8));
        if (opfPath == null) return null;

        // Resolve OPF path relative to container
        String opfDir = "";
        if (opfPath.contains("/")) {
            opfDir = opfPath.substring(0, opfPath.lastIndexOf('/') + 1);
        }

        // Parse OPF to get spine item references
        byte[] opfBytes = files.get(opfPath);
        if (opfBytes == null) return null;

        var opfDoc = parseXml(new String(opfBytes, StandardCharsets.UTF_8));
        if (opfDoc == null) return null;

        // Build id → href mapping from manifest
        Map<String, String> idToHref = new LinkedHashMap<>();
        var manifestElement = getChildElement(opfDoc, "manifest");
        if (manifestElement != null) {
            for (var item : getChildElements(manifestElement, "item")) {
                String id = item.getAttribute("id");
                String href = item.getAttribute("href");
                if (id != null && href != null) {
                    idToHref.put(id, href);
                }
            }
        }

        // Read spine items in order
        StringBuilder text = new StringBuilder();
        var spineElement = getChildElement(opfDoc, "spine");
        if (spineElement != null) {
            for (var itemref : getChildElements(spineElement, "itemref")) {
                String idref = itemref.getAttribute("idref");
                if (idref == null) continue;
                String href = idToHref.get(idref);
                if (href == null) continue;

                String fullPath = opfDir + href;
                byte[] contentBytes = files.get(fullPath);
                if (contentBytes == null) continue;

                String html = new String(contentBytes, StandardCharsets.UTF_8);
                text.append(stripHtml(html)).append("\n\n");
            }
        }

        return text.toString().trim();
    }

    private String parseOpfPathFromContainer(String xml) throws Exception {
        var doc = parseXml(xml);
        if (doc == null) return null;
        var rootfiles = getChildElement(doc, "rootfiles");
        if (rootfiles == null) return null;
        for (var rootfile : getChildElements(rootfiles, "rootfile")) {
            String path = rootfile.getAttribute("full-path");
            if (path != null) return path;
        }
        return null;
    }

    private org.w3c.dom.Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/namespaces", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
    }

    private org.w3c.dom.Element getChildElement(org.w3c.dom.Document doc, String tagName) {
        return getChildElement(doc.getDocumentElement(), tagName);
    }

    private org.w3c.dom.Element getChildElement(org.w3c.dom.Element parent, String tagName) {
        if (parent == null) return null;
        for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && child.getNodeName().equalsIgnoreCase(tagName)) {
                return (org.w3c.dom.Element) child;
            }
        }
        return null;
    }

    private java.util.List<org.w3c.dom.Element> getChildElements(org.w3c.dom.Element parent, String tagName) {
        java.util.List<org.w3c.dom.Element> result = new java.util.ArrayList<>();
        if (parent == null) return result;
        for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && child.getNodeName().equalsIgnoreCase(tagName)) {
                result.add((org.w3c.dom.Element) child);
            }
        }
        return result;
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        // Remove scripts and styles
        html = html.replaceAll("(?si)<script[^>]*>.*?</script>", " ");
        html = html.replaceAll("(?si)<style[^>]*>.*?</style>", " ");
        // Convert block-level tags to newlines to preserve paragraph structure
        html = html.replaceAll("(?si)</?(?:p|div|h[1-6]|blockquote|li|tr|br|section|article|header|footer|nav)[^>]*>", "\n");
        // Remove remaining HTML tags
        html = html.replaceAll("<[^>]*>", " ");
        // Replace HTML entities
        html = html.replaceAll("&nbsp;", " ")
                   .replaceAll("&amp;", "&")
                   .replaceAll("&lt;", "<")
                   .replaceAll("&gt;", ">")
                   .replaceAll("&quot;", "\"")
                   .replaceAll("&[a-zA-Z]+;", " ");
        // Collapse spaces within lines (but preserve newlines)
        html = html.replaceAll("[ \\t]+", " ");
        // Remove empty lines (lines with only spaces)
        html = html.replaceAll("(?m)^[ \\t]+$", "");
        // Limit consecutive newlines to max 2
        html = html.replaceAll("\\n{3,}", "\n\n");
        return html.trim();
    }

    private java.util.List<Map<String, Object>> extractEpubToc(Map<String, byte[]> files) throws Exception {
        java.util.List<Map<String, Object>> chapters = new java.util.ArrayList<>();

        // Try NCX (EPUB 2)
        for (String path : files.keySet()) {
            if (path.endsWith(".ncx")) {
                String xml = new String(files.get(path), StandardCharsets.UTF_8);
                var doc = parseXml(xml);
                if (doc == null) continue;
                parseNcxNavMap(doc, chapters);
                if (!chapters.isEmpty()) return chapters;
            }
        }

        // Try nav.xhtml (EPUB 3)
        for (String path : files.keySet()) {
            String lower = path.toLowerCase();
            if (lower.contains("nav") && (lower.endsWith(".xhtml") || lower.endsWith(".html"))) {
                String xml = new String(files.get(path), StandardCharsets.UTF_8);
                var doc = parseXml(xml);
                if (doc == null) continue;
                parseNavXhtml(doc, chapters);
                if (!chapters.isEmpty()) return chapters;
            }
        }

        return chapters;
    }

    private void parseNcxNavMap(org.w3c.dom.Document doc, java.util.List<Map<String, Object>> chapters) {
        var ncx = doc.getDocumentElement();
        if (ncx == null) return;
        var navMap = getChildElement(ncx, "navMap");
        if (navMap == null) return;
        parseNcxNavPoints(navMap, chapters);
    }

    private void parseNcxNavPoints(org.w3c.dom.Element parent, java.util.List<Map<String, Object>> chapters) {
        for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) continue;
            if (!child.getNodeName().equalsIgnoreCase("navPoint")) continue;
            var navPoint = (org.w3c.dom.Element) child;

            String title = "";
            var navLabel = getChildElement(navPoint, "navLabel");
            if (navLabel != null) {
                var text = getChildElement(navLabel, "text");
                if (text != null && text.getTextContent() != null) {
                    title = text.getTextContent().trim();
                }
            }
            if (!title.isEmpty()) {
                Map<String, Object> chapter = new LinkedHashMap<>();
                chapter.put("title", title);
                chapter.put("level", 1);
                chapters.add(chapter);
            }

            // Parse child navPoints (sub-sections)
            parseNcxNavPoints(navPoint, chapters);
        }
    }

    private void parseNavXhtml(org.w3c.dom.Document doc, java.util.List<Map<String, Object>> chapters) {
        // Look for <nav> element with TOC role
        var body = getChildElement(doc, "html");
        if (body == null) body = doc.getDocumentElement();
        parseNavList(body, chapters, 1);
    }

    private void parseNavList(org.w3c.dom.Element parent, java.util.List<Map<String, Object>> chapters, int level) {
        if (parent == null) return;
        // Find all <ol>/<ul> and their <li> children
        for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) continue;
            var el = (org.w3c.dom.Element) child;
            String tag = el.getNodeName().toLowerCase();

            if (tag.equals("a")) {
                String title = el.getTextContent();
                if (title != null) {
                    title = title.trim();
                    if (!title.isEmpty()) {
                        Map<String, Object> chapter = new LinkedHashMap<>();
                        chapter.put("title", title);
                        chapter.put("level", level);
                        chapters.add(chapter);
                    }
                }
            }

            // Recurse into list items
            if (tag.equals("li") || tag.equals("ol") || tag.equals("ul") || tag.equals("nav")) {
                int nextLevel = tag.equals("li") ? level : level;
                parseNavList(el, chapters, nextLevel);
            }
        }
    }

    private java.util.List<Map<String, Object>> extractSpineFallback(Map<String, byte[]> files) throws Exception {
        java.util.List<Map<String, Object>> chapters = new java.util.ArrayList<>();

        // Find OPF and parse spine items
        byte[] containerBytes = files.get("META-INF/container.xml");
        if (containerBytes == null) return chapters;

        String opfPath = parseOpfPathFromContainer(new String(containerBytes, StandardCharsets.UTF_8));
        if (opfPath == null) return chapters;

        String opfDir = "";
        if (opfPath.contains("/")) {
            opfDir = opfPath.substring(0, opfPath.lastIndexOf('/') + 1);
        }

        byte[] opfBytes = files.get(opfPath);
        if (opfBytes == null) return chapters;

        var opfDoc = parseXml(new String(opfBytes, StandardCharsets.UTF_8));
        if (opfDoc == null) return chapters;

        // Build id→href mapping from manifest
        Map<String, String> idToHref = new LinkedHashMap<>();
        var manifestElement = getChildElement(opfDoc, "manifest");
        if (manifestElement != null) {
            for (var item : getChildElements(manifestElement, "item")) {
                String id = item.getAttribute("id");
                String href = item.getAttribute("href");
                if (id != null && href != null) {
                    idToHref.put(id, href);
                }
            }
        }

        // Read spine items and extract headings
        var spineElement = getChildElement(opfDoc, "spine");
        if (spineElement != null) {
            for (var itemref : getChildElements(spineElement, "itemref")) {
                String idref = itemref.getAttribute("idref");
                if (idref == null) continue;
                String href = idToHref.get(idref);
                if (href == null) continue;

                String fullPath = opfDir + href;
                byte[] contentBytes = files.get(fullPath);
                if (contentBytes == null) continue;

                // Extract first h1-h2 as chapter title
                String html = new String(contentBytes, StandardCharsets.UTF_8);
                String heading = extractFirstHeading(html);
                if (heading != null && !heading.isEmpty()) {
                    Map<String, Object> chapter = new LinkedHashMap<>();
                    chapter.put("title", heading);
                    chapter.put("level", 1);
                    chapters.add(chapter);
                }
            }
        }
        return chapters;
    }

    private String extractFirstHeading(String html) {
        // Find first h1 or h2 tag content
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("<h[12][^>]*>(.*?)</h[12]>", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL)
                .matcher(html);
        if (m.find()) {
            String title = m.group(1).replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
            if (!title.isEmpty()) return title;
        }
        return null;
    }
}
