package com.yuki.shopping.modules.order.task;

import com.yuki.shopping.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderService orderService;

    /** 每分钟扫描：超时未支付（30 分钟）自动取消并释放库存 */
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void cancelTimeoutOrders() {
        int cancelled = orderService.cancelTimeoutOrders(30);
        if (cancelled > 0) {
            log.info("超时未支付订单自动取消 {} 单", cancelled);
        }
    }
}
