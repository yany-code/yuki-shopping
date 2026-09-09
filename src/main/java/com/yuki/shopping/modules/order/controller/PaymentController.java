package com.yuki.shopping.modules.order.controller;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.modules.order.domain.PaymentCallbackDTO;
import com.yuki.shopping.modules.order.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/callback")
    public ApiResponse<Void> callback(@Valid @RequestBody PaymentCallbackDTO payload) {
        paymentService.handleCallback(payload);
        return ApiResponse.ok();
    }
}
