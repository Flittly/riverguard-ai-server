package io.riverguard.common.exception;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 token 已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),

    USERNAME_EXISTS(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    USER_DISABLED(1003, "用户已被禁用"),
    BAD_CREDENTIALS(1004, "用户名或密码错误"),
    ROLE_NOT_IN_SCOPE(1005, "无权限操作该角色"),

    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
