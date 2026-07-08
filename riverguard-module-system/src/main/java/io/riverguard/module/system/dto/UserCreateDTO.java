package io.riverguard.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 64, message = "用户名长度 2-64")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度 6-32")
    private String password;

    @Size(max = 64, message = "昵称最长 64 字符")
    private String nickname;

    @Size(max = 20, message = "手机号最长 20 字符")
    private String phone;

    @Size(max = 128, message = "邮箱最长 128 字符")
    private String email;
}
