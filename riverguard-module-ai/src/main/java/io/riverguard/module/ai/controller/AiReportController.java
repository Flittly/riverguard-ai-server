package io.riverguard.module.ai.controller;


import io.riverguard.common.result.R;
import io.riverguard.module.ai.dto.ReportRequest;
import io.riverguard.module.ai.service.AiReportManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiReportController {

    private final AiReportManager aiReportManager;

    @PostMapping("/report/generate")
    public R<Map<String, Object>> generateReport(@RequestBody @Valid ReportRequest request) {
        String content = aiReportManager.generateReport(request.getTopic(), request.getRequirements());
        return R.ok(Map.of("topic", request.getTopic(), "content", content));
    }
}
