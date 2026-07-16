package io.riverguard.module.ai.service;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.formatter.DeepSeekFormatter;
import io.riverguard.module.ai.config.AiConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AiReportManager {

    private final AiConfig config;
    private OpenAIChatModel model;

    @PostConstruct
    public void init(){
        AiConfig.DeepSeek ds = config.getDeepseek();
        this.model = OpenAIChatModel.builder()
                .apiKey(ds.getApiKey())
                .baseUrl(ds.getBaseUrl())
                .modelName(ds.getModel())
                .formatter(new DeepSeekFormatter())
                .stream(false)
                .build();
        log.info("AI Report model initialized: {}", ds.getModel());
    }

    public String generateReport(String topic, String requirements) {
        ReActAgent agent = ReActAgent.builder()
                .name("ReportGenerator")
                .sysPrompt("你是一个专业的报告生成助手，请根据用户提供的主题和要求生成结构化报告。")
                .model(model)
                .toolkit(new Toolkit())
                .maxIters(3)
                .build();
        String prompt = buildPrompt(topic, requirements);
        Msg response = agent.call(new UserMessage(prompt)).block();
        return response != null ? response.getTextContent() : "";
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
