package com.yuki.shopping.modules.auth.controller;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.modules.auth.domain.LoginDTO;
import com.yuki.shopping.modules.auth.domain.RefreshDTO;
import com.yuki.shopping.modules.auth.domain.RegisterDTO;
import com.yuki.shopping.modules.auth.service.AuthService;
import com.yuki.shopping.modules.auth.domain.TokenVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<TokenVO> login(@Valid @RequestBody LoginDTO request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<TokenVO> register(@Valid @RequestBody RegisterDTO request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenVO> refresh(@Valid @RequestBody RefreshDTO request) {
        return ApiResponse.ok(authService.refresh(request));
    }

}
