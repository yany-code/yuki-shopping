package com.yuki.shopping.common.security;

/** 登录主体类型：客户端用户 / 后台管理员，写入 JWT 的 userType claim */
public enum UserType {
    USER, ADMIN
}
