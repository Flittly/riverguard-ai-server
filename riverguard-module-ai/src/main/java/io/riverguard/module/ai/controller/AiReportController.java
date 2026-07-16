package io.riverguard.module.ai.controller;

import io.riverguard.common.result.R;
import io.riverguard.module.ai.dto.ReportRequest;
import io.riverguard.module.ai.service.AiReportManager;
import io.riverguard.module.ai.service.ReportStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiReportController {

    private final AiReportManager aiReportManager;
    private final ReportStorageService storageService;

    @PostMapping("/report/generate")
    public R<Map<String, Object>> generateReport(@RequestBody @Valid ReportRequest request) {
        Map<String, Object> result = aiReportManager.generateReport("system", UUID.randomUUID().toString(),
                request.getTopic(), request.getRequirements());
        return R.ok(result);
    }

    @GetMapping("/report/list")
    public R<List<Map<String, String>>> listReports() {
        return R.ok(storageService.listReports());
    }

    @GetMapping("/report/{reportId}")
    public R<Map<String, Object>> getReport(@PathVariable String reportId) {
        try {
            return R.ok(storageService.readReport(reportId));
        } catch (Exception e) {
            log.error("Failed to read report: {}", reportId, e);
            return R.fail(500, "报告读取失败");
        }
    }

    @PutMapping("/report/{reportId}")
    public R<Void> updateReport(@PathVariable String reportId, @RequestBody Map<String, String> body) {
        try {
            storageService.updateReport(reportId, body.get("content"));
            return R.ok();
        } catch (Exception e) {
            log.error("Failed to update report: {}", reportId, e);
            return R.fail(500, "报告更新失败");
        }
    }

    @GetMapping("/report/{reportId}/export")
    public ResponseEntity<byte[]> exportReport(@PathVariable String reportId) {
        try {
            byte[] doc = storageService.exportToDoc(reportId);
            String filename = reportId + ".doc";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/msword"))
                    .body(doc);
        } catch (Exception e) {
            log.error("Failed to export report: {}", reportId, e);
            return ResponseEntity.status(500).build();
        }
    }
}
