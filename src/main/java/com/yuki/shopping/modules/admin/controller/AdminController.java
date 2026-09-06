package com.yuki.shopping.modules.admin.controller;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.common.api.PageResult;
import com.yuki.shopping.modules.admin.domain.OrderShipDTO;
import com.yuki.shopping.modules.admin.domain.ReviewStatusDTO;
import com.yuki.shopping.modules.order.domain.OrderListVO;
import com.yuki.shopping.modules.product.domain.ProductListVO;
import com.yuki.shopping.modules.product.domain.ReviewVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/products")
    public ApiResponse<PageResult<ProductListVO>> products(@RequestParam(defaultValue = "1") long page,
                                                           @RequestParam(defaultValue = "20") long pageSize) {
        // TODO 待接入商品服务
        return ApiResponse.ok(new PageResult<>(List.of(), page, pageSize, 0));
    }

    @GetMapping("/orders")
    public ApiResponse<PageResult<OrderListVO>> orders(@RequestParam(defaultValue = "1") long page,
                                                       @RequestParam(defaultValue = "20") long pageSize) {
        // TODO 待接入订单服务
        return ApiResponse.ok(new PageResult<>(List.of(), page, pageSize, 0));
    }

    @PostMapping("/orders/{orderNo}/ship")
    public ApiResponse<Void> ship(@PathVariable String orderNo, @Valid @RequestBody OrderShipDTO request) {
        // TODO 待接入订单服务
        return ApiResponse.ok();
    }

    @GetMapping("/reviews")
    public ApiResponse<PageResult<ReviewVO>> reviews(@RequestParam(defaultValue = "1") long page,
                                                     @RequestParam(defaultValue = "20") long pageSize) {
        // TODO 待接入评价服务
        return ApiResponse.ok(new PageResult<>(List.of(), page, pageSize, 0));
    }

    @PutMapping("/reviews/{id}/status")
    public ApiResponse<Void> reviewStatus(@PathVariable Long id, @Valid @RequestBody ReviewStatusDTO request) {
        // TODO 待接入评价服务
        return ApiResponse.ok();
    }
}
