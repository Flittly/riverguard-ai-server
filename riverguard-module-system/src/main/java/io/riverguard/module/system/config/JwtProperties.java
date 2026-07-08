package io.riverguard.module.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "riverguard-default-secret-key-must-be-at-least-256-bits-long";
    private long expiration = 86400000;
}
