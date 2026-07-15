package io.riverguard.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "消息不能为空")
    @Size(max = 5000, message = "消息长度不能超过5000字符")
    private String message;

    private String sessionId;
}
