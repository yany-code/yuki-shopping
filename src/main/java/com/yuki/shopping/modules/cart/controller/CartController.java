package com.yuki.shopping.modules.cart.controller;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.modules.cart.domain.CartCheckAllDTO;
import com.yuki.shopping.modules.cart.domain.CartItemAddDTO;
import com.yuki.shopping.modules.cart.domain.CartItemUpdateDTO;
import com.yuki.shopping.modules.cart.domain.CartItemVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @GetMapping
    public ApiResponse<List<CartItemVO>> list() {
        // TODO 待接入购物车服务
        return ApiResponse.ok(List.of());
    }

    @PostMapping("/items")
    public ApiResponse<CartItemVO> add(@Valid @RequestBody CartItemAddDTO request) {
        // TODO 待接入购物车服务
        return ApiResponse.ok();
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartItemVO> update(@PathVariable Long id, @Valid @RequestBody CartItemUpdateDTO request) {
        // TODO 待接入购物车服务
        return ApiResponse.ok();
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        // TODO 待接入购物车服务
        return ApiResponse.ok();
    }

    @PutMapping("/items/check-all")
    public ApiResponse<Void> checkAll(@Valid @RequestBody CartCheckAllDTO request) {
        // TODO 待接入购物车服务
        return ApiResponse.ok();
    }
}
