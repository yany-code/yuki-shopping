package com.yuki.shopping.modules.user.controller;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.modules.user.domain.AddressDTO;
import com.yuki.shopping.modules.user.domain.UserProfileDTO;
import com.yuki.shopping.modules.user.domain.AddressVO;
import com.yuki.shopping.modules.user.domain.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/me")
public class UserController {

    @GetMapping
    public ApiResponse<UserVO> me() {
        // TODO 待接入用户服务
        return ApiResponse.ok();
    }

    @PutMapping
    public ApiResponse<UserVO> update(@Valid @RequestBody UserProfileDTO request) {
        // TODO 待接入用户服务
        return ApiResponse.ok();
    }

    @GetMapping("/addresses")
    public ApiResponse<List<AddressVO>> addresses() {
        // TODO 待接入地址服务
        return ApiResponse.ok(List.of());
    }

    @PostMapping("/addresses")
    public ApiResponse<AddressVO> add(@Valid @RequestBody AddressDTO request) {
        // TODO 待接入地址服务
        return ApiResponse.ok();
    }

    @PutMapping("/addresses/{id}")
    public ApiResponse<AddressVO> edit(@PathVariable Long id, @Valid @RequestBody AddressDTO request) {
        // TODO 待接入地址服务
        return ApiResponse.ok();
    }

    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        // TODO 待接入地址服务
        return ApiResponse.ok();
    }

    @PutMapping("/addresses/{id}/default")
    public ApiResponse<Void> setDefault(@PathVariable Long id) {
        // TODO 待接入地址服务
        return ApiResponse.ok();
    }

}
