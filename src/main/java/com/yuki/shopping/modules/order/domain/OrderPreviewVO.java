package com.yuki.shopping.modules.order.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 下单试算/预览结果 */
@Data
public class OrderPreviewVO {

    /** 结算商品明细 */
    private List<OrderItemVO> items;

    private BigDecimal totalAmount;

    private BigDecimal freightAmount;

    /** 应付金额 = 总金额 + 运费 - 优惠 */
    private BigDecimal payAmount;
}
