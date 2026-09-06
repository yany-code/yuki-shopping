package com.yuki.shopping.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HS256 密钥，至少 32 字节 */
    private String secret;

    private String issuer = "yuki-shopping";

    private long accessTokenTtlMinutes = 120;

    private long refreshTokenTtlDays = 7;
}
