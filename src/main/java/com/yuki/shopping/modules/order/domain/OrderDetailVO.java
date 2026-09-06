package com.yuki.shopping.modules.order.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailVO {

    private String orderNo;

    private Integer status;

    private BigDecimal totalAmount;

    private BigDecimal freightAmount;

    private BigDecimal discountAmount;

    /** 实付金额 = 总金额 + 运费 - 优惠 */
    private BigDecimal payAmount;

    /** 收货信息为下单时刻快照 */
    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String remark;

    private String expressCompany;

    private String expressNo;

    private LocalDateTime payTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime finishTime;

    private LocalDateTime createdAt;

    /** 订单商品明细 */
    private List<OrderItemVO> items;
}
