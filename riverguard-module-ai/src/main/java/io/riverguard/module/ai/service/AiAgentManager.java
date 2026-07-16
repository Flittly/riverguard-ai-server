package io.riverguard.module.ai.service;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.formatter.DeepSeekFormatter;
import io.agentscope.harness.agent.HarnessAgent;
import io.riverguard.module.ai.config.AiConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAgentManager {

    private final AiConfig config;
    private HarnessAgent agent;

    @PostConstruct
    public void init() {
        AiConfig.DeepSeek ds = config.getDeepseek();
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(ds.getApiKey())
                .baseUrl(ds.getBaseUrl())
                .modelName(ds.getModel())
                .formatter(new DeepSeekFormatter())
                .stream(true)
                .build();

        this.agent = HarnessAgent.builder()
                .name("RiverGuard-AI")
                .sysPrompt("你是长江崩岸监测预警系统的AI助手，请以专业、简洁的方式回答用户问题。")
                .model(model)
                .toolkit(new Toolkit())
                .maxIters(5)
                .workspace(Path.of(System.getProperty("java.io.tmpdir"), "riverguard-ai-workspace"))
                .build();
        log.info("AI Agent initialized with model: {}", ds.getModel());
    }

    public Flux<AgentEvent> chatStream(String userId, String sessionId, String message) {
        RuntimeContext ctx = RuntimeContext.builder()
                .userId(userId)
                .sessionId(sessionId)
                .build();
        return agent.streamEvents(message, ctx);
    }
}
