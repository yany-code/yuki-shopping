package com.yuki.shopping.common.web;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.common.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> business(BusinessException e) { return ApiResponse.fail(e.getCode(), e.getMessage()); }
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ApiResponse<Void> validation(Exception e) { return ApiResponse.fail(40000, "请求参数错误"); }
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> unknown(Exception e) { return ApiResponse.fail(50000, "系统异常"); }
}
