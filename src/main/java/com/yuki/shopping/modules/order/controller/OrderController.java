package com.yuki.shopping.modules.order.controller;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.common.api.PageResult;
import com.yuki.shopping.modules.order.domain.CreateOrderResult;
import com.yuki.shopping.modules.order.domain.OrderCreateDTO;
import com.yuki.shopping.modules.order.domain.OrderDetailVO;
import com.yuki.shopping.modules.order.domain.OrderListVO;
import com.yuki.shopping.modules.order.domain.OrderPayDTO;
import com.yuki.shopping.modules.order.domain.OrderPreviewVO;
import com.yuki.shopping.modules.order.domain.PayResultVO;
import com.yuki.shopping.modules.order.service.OrderService;
import com.yuki.shopping.modules.order.service.PaymentService;
import com.yuki.shopping.modules.product.domain.ReviewDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @PostMapping("/preview")
    public ApiResponse<OrderPreviewVO> preview(@Valid @RequestBody OrderCreateDTO request) {
        return ApiResponse.ok(orderService.preview(request));
    }

    @PostMapping
    public ApiResponse<CreateOrderResult> create(@Valid @RequestBody OrderCreateDTO request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ApiResponse.ok(orderService.create(request, idempotencyKey));
    }

    @GetMapping
    public ApiResponse<PageResult<OrderListVO>> page(@RequestParam(defaultValue = "1") long page,
                                                     @RequestParam(defaultValue = "20") long pageSize,
                                                     @RequestParam(required = false) Integer status) {
        return ApiResponse.ok(orderService.page(page, pageSize, status));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<OrderDetailVO> detail(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.detail(orderNo));
    }

    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<Void> cancel(@PathVariable String orderNo) {
        orderService.cancel(orderNo);
        return ApiResponse.ok();
    }

    @PostMapping("/{orderNo}/confirm")
    public ApiResponse<Void> confirm(@PathVariable String orderNo) {
        orderService.confirm(orderNo);
        return ApiResponse.ok();
    }

    @PostMapping("/{orderNo}/pay")
    public ApiResponse<PayResultVO> pay(@PathVariable String orderNo, @Valid @RequestBody OrderPayDTO request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ApiResponse.ok(paymentService.pay(orderNo, request, idempotencyKey));
    }

    @PostMapping("/{orderNo}/items/{itemId}/review")
    public ApiResponse<Void> review(@PathVariable String orderNo, @PathVariable Long itemId,
                                    @Valid @RequestBody ReviewDTO request) {
        // TODO 待接入评价服务（阶段 5）
        return ApiResponse.ok();
    }
}
