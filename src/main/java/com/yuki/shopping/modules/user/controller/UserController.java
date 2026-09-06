package com.yuki.shopping.modules.user.controller;

import com.yuki.shopping.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/users/me")
public class UserController {

    public record ProfileRequest(@Size(max=50) String nickname, @Size(max=20) String phone, @Email String email, String avatar) {}
    public record AddressRequest(@NotBlank String receiverName, @NotBlank String receiverPhone, @NotBlank String province,
                                 @NotBlank String city, @NotBlank String district, @NotBlank String detail, Boolean isDefault) {}

    @GetMapping
    public ApiResponse<?> me() {
        return ApiResponse.ok();
    }

    @PutMapping
    public ApiResponse<?> update(@Valid @RequestBody ProfileRequest request) {
        return ApiResponse.ok(request);
    }

    @GetMapping("/addresses")
    public ApiResponse<?> addresses() {
        return ApiResponse.ok(java.util.List.of());
    }

    @PostMapping("/addresses")
    public ApiResponse<?> add(@Valid @RequestBody AddressRequest request) {
        return ApiResponse.ok(request);
    }

    @PutMapping("/addresses/{id}")
    public ApiResponse<?> edit(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return ApiResponse.ok(request);
    }

    @DeleteMapping("/addresses/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        return ApiResponse.ok();
    }

    @PutMapping("/addresses/{id}/default")
    public ApiResponse<?> setDefault(@PathVariable Long id) {
        return ApiResponse.ok();
    }

}
