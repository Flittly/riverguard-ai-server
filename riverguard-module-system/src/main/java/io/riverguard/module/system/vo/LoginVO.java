package io.riverguard.module.system.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LoginVO {

    private String token;
    private UserVO userInfo;
}
