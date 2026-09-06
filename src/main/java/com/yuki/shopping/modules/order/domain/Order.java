package com.yuki.shopping.modules.order.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class Order {

    /** 状态机：10待支付 -> 20已支付待发货 -> 30已发货 -> 40已完成；10可转50已取消；20/30可转60已退款 */
    public static final int STATUS_UNPAID = 10;
    public static final int STATUS_PAID = 20;
    public static final int STATUS_SHIPPED = 30;
    public static final int STATUS_FINISHED = 40;
    public static final int STATUS_CANCELED = 50;
    public static final int STATUS_REFUNDED = 60;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号(业务唯一，应用层生成) */
    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    private BigDecimal freightAmount;

    private BigDecimal discountAmount;

    /** 实付金额 = 总金额 + 运费 - 优惠 */
    private BigDecimal payAmount;

    private Integer status;

    /** 以下三项为下单时刻收货信息快照，不随用户修改地址变化 */
    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String remark;

    private String expressCompany;

    private String expressNo;

    private LocalDateTime payTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime finishTime;

    private LocalDateTime cancelTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private Integer deleted;
}
