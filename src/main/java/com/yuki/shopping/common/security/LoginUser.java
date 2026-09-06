package com.yuki.shopping.common.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/** JWT 解析出的登录主体，作为 SecurityContext 的 principal */
public record LoginUser(Long userId, String username, UserType userType, Integer role) {

    public boolean isAdmin() { return userType == UserType.ADMIN; }

    /** 管理员角色：1-普通管理员 2-超级管理员，仅 userType=ADMIN 时有意义 */
    public boolean isSuperAdmin() {
        return isAdmin() && Integer.valueOf(2).equals(role);
    }

    public List<SimpleGrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority(isAdmin() ? "ROLE_ADMIN" : "ROLE_USER"));
    }
}
