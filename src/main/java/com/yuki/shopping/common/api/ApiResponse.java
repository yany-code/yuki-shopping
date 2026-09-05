package com.yuki.shopping.common.api;

import java.util.UUID;
public record ApiResponse<T>(int code, String message, T data, String traceId) {
    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(0, "ok", data, UUID.randomUUID().toString()); }
    public static <T> ApiResponse<T> ok() { return ok(null); }
    public static <T> ApiResponse<T> fail(int code, String message) { return new ApiResponse<>(code, message, null, UUID.randomUUID().toString()); }
}
