package com.yuki.shopping.modules.order.service;

import com.yuki.shopping.modules.order.domain.OrderPayDTO;
import com.yuki.shopping.modules.order.domain.PaymentCallbackDTO;
import com.yuki.shopping.modules.order.domain.PayResultVO;

public interface PaymentService {

    PayResultVO pay(String orderNo, OrderPayDTO request, String idempotencyKey);

    void handleCallback(PaymentCallbackDTO payload);
}
