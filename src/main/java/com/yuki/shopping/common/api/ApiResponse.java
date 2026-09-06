package com.yuki.shopping.common.api;

import com.yuki.shopping.common.web.TraceIdFilter;
import org.slf4j.MDC;

import java.util.UUID;
public record ApiResponse<T>(int code, String message, T data, String traceId) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data, currentTraceId());
    }
    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null, currentTraceId());
    }

    /** 优先取请求级 traceId（TraceIdFilter 写入 MDC）；无请求上下文时（如定时任务）退化为随机值 */
    private static String currentTraceId() {
        String traceId = MDC.get(TraceIdFilter.MDC_KEY);
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }
}
