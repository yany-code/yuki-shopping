package com.yuki.shopping.modules.order.controller;

import com.yuki.shopping.common.api.ApiResponse;
import com.yuki.shopping.modules.order.domain.PaymentCallbackDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @PostMapping("/callback")
    public ApiResponse<Void> callback(@Valid @RequestBody PaymentCallbackDTO payload) {
        // TODO 校验签名并回写支付单状态
        return ApiResponse.ok();
    }

}
