package io.riverguard.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportRequest {

    @NotBlank(message = "报告主题不能为空")
    private String topic;

    private String requirements;
}
