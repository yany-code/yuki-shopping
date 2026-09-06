package com.yuki.shopping.modules.order.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/** 第三方支付回调请求体，字段以支付网关实际推送为准 */
@Data
public class PaymentCallbackDTO {

    /** 支付流水号(系统生成) */
    @NotBlank
    private String paymentNo;

    /** 支付金额，网关以字符串传输，BigDecimal 可直接解析 */
    @NotNull
    private BigDecimal amount;

    @NotNull
    private Integer status;

    /** 第三方支付交易号 */
    private String tradeNo;
}
