package com.yuki.shopping.modules.order.service;

import com.yuki.shopping.common.api.PageResult;
import com.yuki.shopping.modules.order.domain.OrderCreateDTO;
import com.yuki.shopping.modules.order.domain.OrderDetailVO;
import com.yuki.shopping.modules.order.domain.OrderListVO;
import com.yuki.shopping.modules.order.domain.OrderPreviewVO;
import com.yuki.shopping.modules.order.domain.CreateOrderResult;

public interface OrderService {

    OrderPreviewVO preview(OrderCreateDTO request);

    CreateOrderResult create(OrderCreateDTO request, String idempotencyKey);

    PageResult<OrderListVO> page(long page, long pageSize, Integer status);

    OrderDetailVO detail(String orderNo);

    void cancel(String orderNo);

    void confirm(String orderNo);

    /** 定时任务入口：取消超时未支付订单，返回取消数量 */
    int cancelTimeoutOrders(int timeoutMinutes);
}
