package com.yuki.shopping.modules.order.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderPayDTO {

    /** 支付方式 1-模拟支付(Demo) 2-支付宝 3-微信 */
    @NotNull
    private Integer payType;
}
