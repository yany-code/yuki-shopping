package com.yuki.shopping.modules.order.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderListVO {

    private String orderNo;

    private Integer status;

    private BigDecimal totalAmount;

    private BigDecimal freightAmount;

    private BigDecimal payAmount;

    private LocalDateTime createdAt;

    /** 订单商品明细 */
    private List<OrderItemVO> items;
}
