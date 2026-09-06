package com.yuki.shopping.modules.order.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/** 发起支付结果 */
@Data
public class PayResultVO {

    /** 支付流水号(系统生成) */
    private String paymentNo;

    /** 支付方式 1-模拟支付(Demo) 2-支付宝 3-微信 */
    private Integer payType;

    /** 第三方支付参数，模拟支付为空 */
    private Map<String, String> payParams;

    /** 支付单过期时间 */
    private LocalDateTime expireTime;
}
