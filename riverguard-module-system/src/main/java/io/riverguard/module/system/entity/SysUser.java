package io.riverguard.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.riverguard.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private Integer status;
}
