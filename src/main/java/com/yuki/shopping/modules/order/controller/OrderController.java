package com.yuki.shopping.modules.order.controller;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.common.api.PageResult;
import com.yuki.shopping.modules.order.domain.OrderCreateDTO;
import com.yuki.shopping.modules.order.domain.OrderPayDTO;
import com.yuki.shopping.modules.order.domain.CreateOrderResult;
import com.yuki.shopping.modules.order.domain.OrderDetailVO;
import com.yuki.shopping.modules.order.domain.OrderListVO;
import com.yuki.shopping.modules.order.domain.OrderPreviewVO;
import com.yuki.shopping.modules.order.domain.PayResultVO;
import com.yuki.shopping.modules.product.domain.ReviewDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @PostMapping("/preview")
    public ApiResponse<OrderPreviewVO> preview(@Valid @RequestBody OrderCreateDTO request) {
        // TODO 待接入订单服务
        return ApiResponse.ok();
    }

    @PostMapping
    public ApiResponse<CreateOrderResult> create(@Valid @RequestBody OrderCreateDTO request) {
        // TODO 待接入订单服务(事务 + 幂等)
        return ApiResponse.ok();
    }

    @GetMapping
    public ApiResponse<PageResult<OrderListVO>> page(@RequestParam(defaultValue = "1") long page,
                                                     @RequestParam(defaultValue = "20") long pageSize,
                                                     @RequestParam(required = false) Integer status) {
        // TODO 待接入订单服务
        return ApiResponse.ok(new PageResult<>(List.of(), page, pageSize, 0));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<OrderDetailVO> detail(@PathVariable String orderNo) {
        // TODO 待接入订单服务
        return ApiResponse.ok();
    }

    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<Void> cancel(@PathVariable String orderNo) {
        // TODO 待接入订单服务
        return ApiResponse.ok();
    }

    @PostMapping("/{orderNo}/confirm")
    public ApiResponse<Void> confirm(@PathVariable String orderNo) {
        // TODO 待接入订单服务
        return ApiResponse.ok();
    }

    @PostMapping("/{orderNo}/pay")
    public ApiResponse<PayResultVO> pay(@PathVariable String orderNo, @Valid @RequestBody OrderPayDTO request) {
        // TODO 待接入支付服务
        return ApiResponse.ok();
    }

    @PostMapping("/{orderNo}/items/{itemId}/review")
    public ApiResponse<Void> review(@PathVariable String orderNo, @PathVariable Long itemId,
                                    @Valid @RequestBody ReviewDTO request) {
        // TODO 待接入评价服务
        return ApiResponse.ok();
    }
}
