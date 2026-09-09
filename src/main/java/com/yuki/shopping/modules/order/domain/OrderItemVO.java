package com.yuki.shopping.modules.order.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/** 订单商品明细，字段为下单时刻快照 */
@Data
public class OrderItemVO {

    private Long id;

    private Long productId;

    private Long skuId;

    /** 商品标题(快照) */
    private String productName;

    /** 规格组合(快照)，如 {"颜色":"黑","容量":"256G"}，与购物车/商品详情口径一致 */
    private Map<String, String> skuSpecs;

    /** 图片URL(快照) */
    private String image;

    /** 成交单价(快照) */
    private BigDecimal price;

    private Integer quantity;

    /** 小计 = 单价 × 数量 */
    private BigDecimal subtotal;
}
