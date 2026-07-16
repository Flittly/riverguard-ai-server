package io.riverguard.module.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class ReportStorageService {

    private static final DateTimeFormatter DIR_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${ai.reports.dir:${user.dir}/reports}")
    private String reportsDir;

    private Path basePath;

    @PostConstruct
    public void init() throws IOException {
        this.basePath = Path.of(reportsDir);
        Files.createDirectories(basePath);
        log.info("Report storage initialized at: {}", basePath.toAbsolutePath());
    }

    public record SaveResult(String reportId, String createdAt, Path dirPath) {}

    public SaveResult saveReport(String topic, String content) throws IOException {
        String timestamp = LocalDateTime.now().format(DIR_FMT);
        String hash = Integer.toHexString((topic + System.nanoTime()).hashCode());
        String dirName = timestamp + "_" + hash;
        Path reportDir = basePath.resolve(dirName);
        Files.createDirectories(reportDir);

        Path mdFile = reportDir.resolve("report.md");
        Files.writeString(mdFile, content, StandardCharsets.UTF_8);

        Path metaFile = reportDir.resolve("meta.json");
        Map<String, String> meta = Map.of("topic", topic, "createdAt", timestamp);
        Files.writeString(metaFile, MAPPER.writeValueAsString(meta), StandardCharsets.UTF_8);

        log.info("Report saved: {}", reportDir.getFileName());
        return new SaveResult(dirName, timestamp.replace("_", " "), reportDir);
    }

    public List<Map<String, String>> listReports() {
        List<Map<String, String>> reports = new ArrayList<>();
        if (!Files.exists(basePath)) {
            return reports;
        }
        try (Stream<Path> dirs = Files.list(basePath)) {
            List<Path> sorted = dirs
                    .filter(Files::isDirectory)
                    .sorted(Comparator.reverseOrder())
                    .toList();
            for (Path dir : sorted) {
                Path metaFile = dir.resolve("meta.json");
                Path mdFile = dir.resolve("report.md");
                if (Files.exists(metaFile) && Files.exists(mdFile)) {
                    Map<String, String> meta = MAPPER.readValue(metaFile.toFile(), Map.class);
                    Map<String, String> item = new java.util.LinkedHashMap<>();
                    item.put("id", dir.getFileName().toString());
                    item.put("topic", meta.getOrDefault("topic", ""));
                    item.put("createdAt", meta.getOrDefault("createdAt", ""));
                    String preview = Files.readString(mdFile, StandardCharsets.UTF_8);
                    if (preview.length() > 200) {
                        preview = preview.substring(0, 200) + "...";
                    }
                    item.put("preview", preview);
                    reports.add(item);
                }
            }
        } catch (IOException e) {
            log.error("Failed to list reports", e);
        }
        return reports;
    }

    public Map<String, Object> readReport(String reportId) throws IOException {
        Path reportDir = basePath.resolve(reportId);
        if (!Files.exists(reportDir)) {
            throw new IllegalArgumentException("Report not found: " + reportId);
        }
        Path metaFile = reportDir.resolve("meta.json");
        Path mdFile = reportDir.resolve("report.md");

        Map<String, String> meta = MAPPER.readValue(metaFile.toFile(), Map.class);
        String content = Files.readString(mdFile, StandardCharsets.UTF_8);

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("id", reportId);
        result.put("topic", meta.getOrDefault("topic", ""));
        result.put("createdAt", meta.getOrDefault("createdAt", ""));
        result.put("content", content);
        return result;
    }
}
