package com.yuki.shopping.modules.order.domain;

import com.yuki.shopping.common.exception.BusinessException;

import java.util.Map;
import java.util.Set;

/** 订单状态机：流转合法性统一在此校验，非法流转抛 40900，服务端不依赖前端控制 */
public final class OrderStatusMachine {

    private static final Map<Integer, Set<Integer>> ALLOWED = Map.of(
            Order.STATUS_UNPAID, Set.of(Order.STATUS_PAID, Order.STATUS_CANCELED),
            Order.STATUS_PAID, Set.of(Order.STATUS_SHIPPED, Order.STATUS_REFUNDED),
            Order.STATUS_SHIPPED, Set.of(Order.STATUS_FINISHED, Order.STATUS_REFUNDED),
            Order.STATUS_FINISHED, Set.of(),
            Order.STATUS_CANCELED, Set.of(),
            Order.STATUS_REFUNDED, Set.of());

    private OrderStatusMachine() { }

    public static void checkTransition(int from, int to) {
        if (!ALLOWED.getOrDefault(from, Set.of()).contains(to)) {
            throw new BusinessException(40900, "订单当前状态不允许该操作");
        }
    }
}
