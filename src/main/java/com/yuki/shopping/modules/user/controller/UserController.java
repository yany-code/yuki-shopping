package com.yuki.shopping.modules.user.controller;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.modules.user.domain.AddressDTO;
import com.yuki.shopping.modules.user.domain.UserProfileDTO;
import com.yuki.shopping.modules.user.domain.AddressVO;
import com.yuki.shopping.modules.user.domain.UserVO;
import com.yuki.shopping.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 查询信息
     * @return
     */
    @GetMapping
    public ApiResponse<UserVO> me() {
        return ApiResponse.ok(userService.me());
    }

    /**
     * 更新信息
     * @param request
     * @return
     */
    @PutMapping
    public ApiResponse<UserVO> update(@Valid @RequestBody UserProfileDTO request) {
        return ApiResponse.ok(userService.updateProfile(request));
    }

    /**
     * 查询地址
     * @return
     */
    @GetMapping("/addresses")
    public ApiResponse<List<AddressVO>> addresses() {
        return ApiResponse.ok(userService.listAddresses());
    }

    /**
     * 增加地址
     * @param request
     * @return
     */
    @PostMapping("/addresses")
    public ApiResponse<AddressVO> add(@Valid @RequestBody AddressDTO request) {
        return ApiResponse.ok(userService.addAddress(request));
    }

    /**
     * 更新地址
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/addresses/{id}")
    public ApiResponse<AddressVO> edit(@PathVariable Long id, @Valid @RequestBody AddressDTO request) {
        return ApiResponse.ok(userService.updateAddress(id,request));
    }

    /**
     * 删除地址
     * @param id
     * @return
     */
    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.deleteAddress(id);
        return ApiResponse.ok();
    }

    /**
     * 设置为默认地址
     * @param id
     * @return
     */
    @PutMapping("/addresses/{id}/default")
    public ApiResponse<Void> setDefault(@PathVariable Long id) {
        userService.setDefaultAddress(id);
        return ApiResponse.ok();
    }

}
