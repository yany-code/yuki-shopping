package com.yuki.shopping.common.web;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.common.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> business(BusinessException e) {
        log.warn("业务异常 code={}: {}", e.getCode(), e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> validation(MethodArgumentNotValidException e) {
        var fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? "请求参数错误" : fieldError.getField() + " " + fieldError.getDefaultMessage();
        return ApiResponse.fail(40000, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> validation(ConstraintViolationException e) {
        return ApiResponse.fail(40000, e.getConstraintViolations().stream().findFirst()
                .map(v -> v.getPropertyPath() + " " + v.getMessage()).orElse("请求参数错误"));
    }

    /** 请求体不可读、查询参数类型转换失败等参数级错误 */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ApiResponse<Void> badRequest(Exception e) {
        return ApiResponse.fail(40000, "请求参数错误");
    }

    @ExceptionHandler({NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class})
    public ApiResponse<Void> notFound(Exception e) {
        return ApiResponse.fail(40400, "资源不存在");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ApiResponse<Void> unauthorized(AuthenticationException e) {
        return ApiResponse.fail(40100, "未登录或Token无效");
    }

    /** 方法级鉴权失败（@PreAuthorize）；过滤器链层面由 SecurityConfig 的 accessDeniedHandler 处理 */
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> forbidden(AccessDeniedException e) {
        return ApiResponse.fail(40300, "无权限访问");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> unknown(Exception e) {
        log.error("系统异常", e);
        return ApiResponse.fail(50000, "系统异常");
    }
}
