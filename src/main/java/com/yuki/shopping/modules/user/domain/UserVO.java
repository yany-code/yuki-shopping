package com.yuki.shopping.modules.user.domain;

import lombok.Data;

/** 用户资料视图对象，不暴露 passwordHash 等敏感字段 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String phone;

    private String email;

    private String avatar;
}
