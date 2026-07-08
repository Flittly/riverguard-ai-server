package io.riverguard.module.system.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private Integer status;
    private List<String> roleCodes;
    private List<String> roleNames;
    private LocalDateTime createTime;
}
