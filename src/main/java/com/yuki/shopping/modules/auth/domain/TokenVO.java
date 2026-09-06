package com.yuki.shopping.modules.auth.domain;

import lombok.Data;

/** 登录/注册/刷新令牌返回的令牌信息 */
@Data
public class TokenVO {

    private String tokenType = "Bearer";

    private String accessToken;

    private String refreshToken;

    /** accessToken 有效期(秒) */
    private Long expiresIn;
}
