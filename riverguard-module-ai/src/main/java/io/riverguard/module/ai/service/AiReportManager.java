package io.riverguard.module.ai.service;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.formatter.DeepSeekFormatter;
import io.agentscope.harness.agent.HarnessAgent;
import io.riverguard.module.ai.config.AiConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiReportManager {

    private final AiConfig config;
    private final ReportStorageService storageService;
    private HarnessAgent agent;

    @PostConstruct
    public void init() {
        AiConfig.DeepSeek ds = config.getDeepseek();
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(ds.getApiKey())
                .baseUrl(ds.getBaseUrl())
                .modelName(ds.getModel())
                .formatter(new DeepSeekFormatter())
                .stream(false)
                .build();

        this.agent = HarnessAgent.builder()
                .name("ReportGenerator")
                .sysPrompt("你是一个专业的报告生成助手，请根据用户提供的主题和要求生成结构化报告。")
                .model(model)
                .toolkit(new Toolkit())
                .maxIters(3)
                .workspace(Path.of(System.getProperty("java.io.tmpdir"), "riverguard-report-workspace"))
                .build();
        log.info("AI Report model initialized with model: {}", ds.getModel());
    }

    public Map<String, Object> generateReport(String userId, String sessionId, String topic, String requirements) {
        RuntimeContext ctx = RuntimeContext.builder()
                .userId(userId)
                .sessionId(sessionId)
                .build();
        String prompt = buildPrompt(topic, requirements);
        Msg response = agent.call(prompt, ctx).block();
        String content = response != null ? response.getTextContent() : "";

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("topic", topic);
        result.put("content", content);

        if (!content.isEmpty()) {
            try {
                ReportStorageService.SaveResult saved = storageService.saveReport(topic, content);
                result.put("id", saved.reportId());
                result.put("createdAt", saved.createdAt());
            } catch (IOException e) {
                log.error("Failed to save report", e);
                result.put("id", "error");
                result.put("createdAt", "");
            }
        }
        return result;
    }

    private String buildPrompt(String topic, String requirements) {
        StringBuilder sb = new StringBuilder();
        sb.append("请生成一份关于以下主题的报告：\n\n");
        sb.append("主题：").append(topic).append("\n");
        if (requirements != null && !requirements.isBlank()) {
            sb.append("要求：").append(requirements).append("\n");
        }
        sb.append("\n请以专业、结构化的方式撰写报告，包含标题、摘要、正文和结论。");
        return sb.toString();
    }
}
