package com.weixinlibrary.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
@Service
public class BookContentService {

    private static final Pattern SAFE_FILENAME =
            Pattern.compile("^[a-f0-9\\-]+\\.(txt|epub|pdf|mobi)$", Pattern.CASE_INSENSITIVE);

    @Value("${app.file.upload-dir:./uploads/books}")
    private String uploadDir;

    public String extractText(String filename) throws IOException {
        Path file = resolveFile(filename);
        return switch (extension(filename)) {
            case "txt" -> Files.readString(file, StandardCharsets.UTF_8);
            case "epub" -> extractEpubText(file);
            default -> throw new IllegalArgumentException("暂不支持该格式的文本提取: " + extension(filename));
        };
    }

    public List<Map<String, String>> extractToc(String filename) throws IOException {
        Path file = resolveFile(filename);
        if (!"epub".equals(extension(filename))) {
            return List.of();
        }
        return extractEpubToc(file);
    }

    public CoverImage extractCover(String filename) throws IOException {
        Path file = resolveFile(filename);
        if (!"epub".equals(extension(filename))) {
            throw new IllegalArgumentException("暂不支持该格式的封面提取: " + extension(filename));
        }
        return extractEpubCover(file);
    }

    private CoverImage extractEpubCover(Path file) throws IOException {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            EpubMeta meta = parseEpubMeta(zip);
            String coverHref = meta.coverHref();
            if (coverHref == null) {
                throw new IOException("EPUB 中未找到封面");
            }

            byte[] data = readZipEntry(zip, meta.resolveHref(coverHref));
            if (data == null || data.length == 0) {
                throw new IOException("封面文件为空");
            }

            String mediaType = meta.coverMediaType() != null
                    ? meta.coverMediaType()
                    : guessImageMediaType(coverHref);
            return new CoverImage(data, mediaType);
        }
    }

    private String guessImageMediaType(String href) {
        String lower = href.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "image/jpeg";
    }

    private String extractEpubText(Path file) throws IOException {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            EpubMeta meta = parseEpubMeta(zip);
            StringBuilder sb = new StringBuilder();

            for (String href : meta.spineHrefs()) {
                byte[] data = readZipEntry(zip, meta.resolveHref(href));
                if (data == null || data.length == 0) continue;
                String text = htmlToText(new String(data, StandardCharsets.UTF_8));
                if (!text.isBlank()) {
                    sb.append(text).append("\n\n");
                }
            }

            if (sb.isEmpty()) {
                throw new IOException("EPUB 中未找到可读文本");
            }
            return sb.toString().trim();
        }
    }

    private List<Map<String, String>> extractEpubToc(Path file) throws IOException {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            EpubMeta meta = parseEpubMeta(zip);
            List<Map<String, String>> chapters = new ArrayList<>();

            if (meta.navHref() != null) {
                byte[] navData = readZipEntry(zip, meta.resolveHref(meta.navHref()));
                if (navData != null) {
                    Document doc = Jsoup.parse(new String(navData, StandardCharsets.UTF_8), "", Parser.xmlParser());
                    for (Element link : doc.select("nav[*|type~=(?i)toc] a, nav#toc a, nav.toc a")) {
                        String title = link.text().trim();
                        if (!title.isEmpty()) {
                            chapters.add(Map.of("title", title));
                        }
                    }
                }
            }

            if (chapters.isEmpty() && meta.ncxHref() != null) {
                byte[] ncxData = readZipEntry(zip, meta.resolveHref(meta.ncxHref()));
                if (ncxData != null) {
                    chapters.addAll(parseNcxToc(ncxData));
                }
            }

            return chapters;
        }
    }

    private List<Map<String, String>> parseNcxToc(byte[] ncxData) {
        List<Map<String, String>> chapters = new ArrayList<>();
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            var builder = factory.newDocumentBuilder();
            var doc = builder.parse(new ByteArrayInputStream(ncxData));
            NodeList navPoints = doc.getElementsByTagName("navPoint");
            for (int i = 0; i < navPoints.getLength(); i++) {
                var navPoint = navPoints.item(i);
                NodeList labels = ((org.w3c.dom.Element) navPoint).getElementsByTagName("text");
                if (labels.getLength() > 0) {
                    String title = labels.item(0).getTextContent().trim();
                    if (!title.isEmpty()) {
                        chapters.add(Map.of("title", title));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse NCX toc: {}", e.getMessage());
        }
        return chapters;
    }

    private EpubMeta parseEpubMeta(ZipFile zip) throws IOException {
        byte[] containerXml = readZipEntry(zip, "META-INF/container.xml");
        if (containerXml == null) {
            throw new IOException("无效的 EPUB：缺少 container.xml");
        }

        Document container = Jsoup.parse(new String(containerXml, StandardCharsets.UTF_8), "", Parser.xmlParser());
        Element rootfile = container.selectFirst("rootfile[media-type=application/oebps-package+xml], rootfile");
        if (rootfile == null) {
            throw new IOException("无效的 EPUB：缺少 rootfile");
        }

        String opfPath = rootfile.attr("full-path");
        byte[] opfData = readZipEntry(zip, opfPath);
        if (opfData == null) {
            throw new IOException("无效的 EPUB：缺少 OPF 文件");
        }

        Document opf = Jsoup.parse(new String(opfData, StandardCharsets.UTF_8), "", Parser.xmlParser());
        String opfDir = opfDirectory(opfPath);

        Map<String, String> manifest = new LinkedHashMap<>();
        for (Element item : opf.select("manifest > item")) {
            manifest.put(item.attr("id"), item.attr("href"));
        }

        List<String> spineHrefs = new ArrayList<>();
        for (Element itemref : opf.select("spine > itemref")) {
            String href = manifest.get(itemref.attr("idref"));
            if (href != null) spineHrefs.add(href);
        }

        String navHref = null;
        String ncxHref = null;
        String coverHref = null;
        String coverMediaType = null;
        String coverMetaId = null;

        for (Element meta : opf.select("metadata > meta")) {
            if ("cover".equals(meta.attr("name"))) {
                coverMetaId = meta.attr("content");
            }
        }

        for (Element item : opf.select("manifest > item")) {
            String props = item.attr("properties").toLowerCase(Locale.ROOT);
            String mediaType = item.attr("media-type").toLowerCase(Locale.ROOT);
            String id = item.attr("id");
            String href = item.attr("href");

            if (props.contains("cover-image")) {
                coverHref = href;
                coverMediaType = item.attr("media-type");
            }
            if (coverMetaId != null && coverMetaId.equals(id)) {
                coverHref = href;
                coverMediaType = item.attr("media-type");
            }
            if ("application/x-dtbncx+xml".equals(mediaType)) {
                ncxHref = href;
            }
            if (props.contains("nav")) {
                navHref = href;
            }
        }

        if (coverHref == null) {
            for (Element item : opf.select("manifest > item")) {
                String id = item.attr("id").toLowerCase(Locale.ROOT);
                String href = item.attr("href").toLowerCase(Locale.ROOT);
                String mediaType = item.attr("media-type").toLowerCase(Locale.ROOT);
                if (mediaType.startsWith("image/")
                        && (id.contains("cover") || href.contains("cover"))) {
                    coverHref = item.attr("href");
                    coverMediaType = item.attr("media-type");
                    break;
                }
            }
        }

        return new EpubMeta(opfDir, spineHrefs, navHref, ncxHref, coverHref, coverMediaType);
    }

    private String htmlToText(String html) {
        Document doc = Jsoup.parse(html);
        doc.select("script, style").remove();
        return doc.body() != null ? doc.body().text().trim() : doc.text().trim();
    }

    private byte[] readZipEntry(ZipFile zip, String path) throws IOException {
        ZipEntry entry = zip.getEntry(normalizeZipPath(path));
        if (entry == null) return null;
        try (InputStream in = zip.getInputStream(entry)) {
            return in.readAllBytes();
        }
    }

    private String normalizeZipPath(String path) {
        return path.replace('\\', '/');
    }

    private String opfDirectory(String opfPath) {
        int slash = opfPath.lastIndexOf('/');
        return slash >= 0 ? opfPath.substring(0, slash + 1) : "";
    }

    private Path resolveFile(String filename) throws IOException {
        if (filename == null || !SAFE_FILENAME.matcher(filename).matches()) {
            throw new IllegalArgumentException("无效的文件名");
        }
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path file = base.resolve(filename).normalize();
        if (!file.startsWith(base)) {
            throw new IllegalArgumentException("无效的文件路径");
        }
        if (!Files.isRegularFile(file)) {
            throw new IOException("文件不存在: " + filename);
        }
        return file;
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return "";
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private record EpubMeta(
            String opfDir,
            List<String> spineHrefs,
            String navHref,
            String ncxHref,
            String coverHref,
            String coverMediaType
    ) {
        String resolveHref(String href) {
            if (href == null) return null;
            if (href.startsWith("/")) return href.substring(1);
            return opfDir + href;
        }
    }

    public record CoverImage(byte[] data, String contentType) {}
}
