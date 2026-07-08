package io.riverguard.common.result;

import lombok.Data;

@Data
public class R<T> {

    private int code;
    private String message;
    private T data;

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> R<T> ok(T data) {
        // 静态方法在“类加载”时就已经存在了，而泛型 T 要在“创建对象”时才能确定。
        // 类上的 T 归实例，静态方法不能用；若想静态用泛型，<T> 必须重新写。
        return new R<>(200, "success", data);
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }
}
