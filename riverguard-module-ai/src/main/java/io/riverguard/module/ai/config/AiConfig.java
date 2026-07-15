package io.riverguard.module.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    private DeepSeek deepseek = new DeepSeek();

    @Data
    public static class DeepSeek {
        private String apiKey;
        private String baseUrl = "https://api.deepseek.com/v1";
        private String model = "deepseek-chat";
        private Double temperature = 0.7;
        private Integer maxTokens = 4096;
    }
}
