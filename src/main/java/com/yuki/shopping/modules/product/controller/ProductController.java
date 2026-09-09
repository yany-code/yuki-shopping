package com.yuki.shopping.modules.product.controller;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.common.api.PageResult;
import com.yuki.shopping.modules.product.domain.*;
import com.yuki.shopping.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Provider;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<PageResult<ProductListVO>> page(ProductQuery query) {
        return ApiResponse.ok(productService.page(query));
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailVO> detail(@PathVariable Long productId) {

        return ApiResponse.ok(productService.detail(productId));

    }

    @GetMapping("/{productId}/reviews")
    public ApiResponse<PageResult<ReviewVO>> reviews(@PathVariable Long productId,
                                                     @RequestParam(required = false) Integer rating,
                                                     @RequestParam(defaultValue = "1") long page,
                                                     @RequestParam(defaultValue = "20")long pageSize){
        return ApiResponse.ok(productService.reviewPage(productId,rating,page,pageSize));
    }

}
