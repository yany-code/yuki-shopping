package com.yuki.shopping.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 模拟支付网关参数，密钥经 .env / 环境变量注入，不写进代码与仓库 */
@Data
@ConfigurationProperties(prefix = "pay")
public class PayProperties {

    /** 回调验签密钥，须与网关侧一致 */
    private String secret;

    /** 支付单有效期（分钟），与超时取消时长对齐 */
    private long expireMinutes = 30;
}
