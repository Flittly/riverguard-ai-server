package io.riverguard.module.ai.controller;

import io.riverguard.common.result.R;
import io.riverguard.module.ai.dto.ReportRequest;
import io.riverguard.module.ai.service.AiReportManager;
import io.riverguard.module.ai.service.ReportStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
