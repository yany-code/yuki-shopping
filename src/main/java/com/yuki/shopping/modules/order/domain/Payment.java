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
@TableName("t_payment")
public class Payment {

    /** 支付状态 */
    public static final int STATUS_PROCESSING = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 支付流水号(系统生成，全局唯一) */
    private String paymentNo;

    private Long orderId;

    /** 订单号(冗余) */
    private String orderNo;

    private Long userId;

    /** 支付方式 1-模拟支付(Demo) 2-支付宝 3-微信 */
    private Integer payType;

    private BigDecimal amount;

    /** 0-处理中 1-成功 2-失败 */
    private Integer status;

    /** 第三方支付交易号(回调回填) */
    private String tradeNo;

    private LocalDateTime paidAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
