package io.riverguard.module.ai.service;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.formatter.DeepSeekFormatter;
import io.riverguard.module.ai.config.AiConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAgentManager {

    private final AiConfig config;
    private OpenAIChatModel model;
    private final Map<String, ReActAgent> sessionAgents = new ConcurrentHashMap<>(); //ConcurrentHashMap是线程安全支持高并发的HashMap

    @PostConstruct // PostConstruct注解，将下面的init函数运行完，在Bean初始化完成后执行
    public void init() {
        AiConfig.DeepSeek ds = config.getDeepseek();
        this.model = OpenAIChatModel.builder()
                .apiKey(ds.getApiKey())
                .baseUrl(ds.getBaseUrl())
                .modelName(ds.getModel())
                .formatter(new DeepSeekFormatter())
                .stream(true)
                .build();
        log.info("AI Agent initialized with model: {}", ds.getModel());
    }

    public ReActAgent getOrCreateAgent(String sessionId) {
        return sessionAgents.computeIfAbsent(sessionId, id -> {
            log.info("Creating new agent for session: {}", id);
            return ReActAgent.builder()
                    .name("RiverGuard-AI")
                    .sysPrompt("你是长江崩岸监测预警系统的AI助手，请以专业、简洁的方式回答用户问题。")
                    .model(model)
                    .toolkit(new Toolkit())
                    .maxIters(5)
                    .build();
        });
    }

    public Flux<io.agentscope.core.event.AgentEvent> chatStream(String sessionId, String message) {
        ReActAgent agent = getOrCreateAgent(sessionId);
        return agent.streamEvents(new UserMessage(message));
    }
}
