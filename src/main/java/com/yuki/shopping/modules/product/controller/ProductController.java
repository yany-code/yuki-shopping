package com.yuki.shopping.modules.product.controller;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.common.api.PageResult;
import com.yuki.shopping.modules.product.domain.ProductDetailVO;
import com.yuki.shopping.modules.product.domain.ProductListVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @GetMapping
    public ApiResponse<PageResult<ProductListVO>> page(@RequestParam(defaultValue = "1") long page,
                                                       @RequestParam(defaultValue = "20") long pageSize) {
        // TODO 待接入商品服务
        return ApiResponse.ok(new PageResult<>(List.of(), page, pageSize, 0));
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailVO> detail(@PathVariable Long productId) {
        // TODO 待接入商品服务
        return ApiResponse.ok();
    }
}
