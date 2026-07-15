package io.riverguard.module.ai.controller;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.formatter.DeepSeekFormatter;
import io.riverguard.common.result.R;
import io.riverguard.module.ai.config.AiConfig;
import io.riverguard.module.ai.dto.ReportRequest;
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

    private final AiConfig config;

    @PostMapping("/report/generate")
    public R<Map<String, Object>> generateReport(@RequestBody @Valid ReportRequest request) {
        AiConfig.DeepSeek ds = config.getDeepseek();
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(ds.getApiKey())
                .baseUrl(ds.getBaseUrl())
                .modelName(ds.getModel())
                .formatter(new DeepSeekFormatter())
                .stream(false)
                .build();

        ReActAgent agent = ReActAgent.builder()
                .name("ReportGenerator")
                .sysPrompt("你是一个专业的报告生成助手，请根据用户提供的主题和要求生成结构化报告。")
                .model(model)
                .toolkit(new Toolkit())
                .maxIters(3)
                .build();

        String prompt = buildPrompt(request);
        Msg response = agent.call(new UserMessage(prompt)).block();

        String content = response != null ? response.getTextContent() : "";
        return R.ok(Map.of("topic", request.getTopic(), "content", content));
    }

    private String buildPrompt(ReportRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("请生成一份关于以下主题的报告：\n\n");
        sb.append("主题：").append(request.getTopic()).append("\n");
        if (request.getRequirements() != null && !request.getRequirements().isBlank()) {
            sb.append("要求：").append(request.getRequirements()).append("\n");
        }
        sb.append("\n请以专业、结构化的方式撰写报告，包含标题、摘要、正文和结论。");
        return sb.toString();
    }
}
