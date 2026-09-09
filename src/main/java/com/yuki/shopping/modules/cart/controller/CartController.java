package com.yuki.shopping.modules.cart.controller;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.modules.cart.domain.CartCheckAllDTO;
import com.yuki.shopping.modules.cart.domain.CartItemAddDTO;
import com.yuki.shopping.modules.cart.domain.CartItemUpdateDTO;
import com.yuki.shopping.modules.cart.domain.CartItemVO;
import com.yuki.shopping.modules.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<List<CartItemVO>> list() {
        return ApiResponse.ok(cartService.list());
    }

    @PostMapping("/items")
    public ApiResponse<CartItemVO> add(@Valid @RequestBody CartItemAddDTO request) {
        return ApiResponse.ok(cartService.add(request));
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartItemVO> update(@PathVariable Long id, @Valid @RequestBody CartItemUpdateDTO request) {
        return ApiResponse.ok(cartService.update(id, request));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        cartService.delete(id);
        return ApiResponse.ok();
    }

    @PutMapping("/items/check-all")
    public ApiResponse<Void> checkAll(@Valid @RequestBody CartCheckAllDTO request) {
        cartService.checkAll(request);
        return ApiResponse.ok();
    }
}
