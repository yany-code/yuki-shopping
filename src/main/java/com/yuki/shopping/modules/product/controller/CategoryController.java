package com.yuki.shopping.modules.product.controller;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.modules.product.domain.CategoryTreeVO;
import com.yuki.shopping.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ProductService productService;

    /**
     * 生成分类品牌层级树
     * @return
     */
    @GetMapping("/tree")
    public ApiResponse<List<CategoryTreeVO>> tree() {
        return ApiResponse.ok(productService.categoryTree());
    }

}