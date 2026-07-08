package io.riverguard.module.system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class UserPageQuery {

    @Min(value = 1, message = "页码从 1 开始")
    private Integer page = 1;

    @Min(value = 1, message = "每页至少 1 条")
    @Max(value = 100, message = "每页不超过 100 条")
    private Integer size = 10;

    private String keyword;
    private Integer status;
}
