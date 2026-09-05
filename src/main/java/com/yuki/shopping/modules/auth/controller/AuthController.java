package com.yuki.shopping.modules.auth.controller;

import com.yuki.shopping.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/auth")
public class AuthController {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    @PostMapping("/login") public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) { return ApiResponse.ok(); }
    @PostMapping("/register") public ApiResponse<?> register(@Valid @RequestBody LoginRequest request) { return ApiResponse.ok(); }
    @PostMapping("/refresh") public ApiResponse<?> refresh() { return ApiResponse.ok(); }
}
