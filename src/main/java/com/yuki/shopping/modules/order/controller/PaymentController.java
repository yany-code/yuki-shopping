package com.yuki.shopping.modules.order.controller;

import com.yuki.shopping.common.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @PostMapping("/callback")
    public ApiResponse<?> callback(@RequestBody Map<String,Object> payload) {
        return ApiResponse.ok();
    }

}
