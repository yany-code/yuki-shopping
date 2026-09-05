package com.yuki.shopping.modules.product.controller;

import com.yuki.shopping.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {
    @GetMapping public ApiResponse<?> page(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(java.util.Map.of("list", java.util.List.of(), "page", page, "pageSize", pageSize, "total", 0));
    }
    @GetMapping("/{productId}") public ApiResponse<?> detail(@PathVariable Long productId) { return ApiResponse.ok(); }
}
