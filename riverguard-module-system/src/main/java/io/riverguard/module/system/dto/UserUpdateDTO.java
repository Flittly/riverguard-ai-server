package io.riverguard.module.system.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    private Long id;

    @Size(max = 64, message = "昵称最长 64 字符")
    private String nickname;

    @Size(max = 20, message = "手机号最长 20 字符")
    private String phone;

    @Size(max = 128, message = "邮箱最长 128 字符")
    private String email;
}
