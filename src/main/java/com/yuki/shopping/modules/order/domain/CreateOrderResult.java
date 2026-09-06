package com.yuki.shopping.modules.order.domain;

import lombok.Data;

import java.math.BigDecimal;

/** 创建订单结果，前端依赖其跳转支付或订单详情 */
@Data
public class CreateOrderResult {

    private String orderNo;

    private Integer status;

    private BigDecimal payAmount;
}
