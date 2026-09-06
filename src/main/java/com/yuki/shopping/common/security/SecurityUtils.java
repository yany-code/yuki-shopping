package com.yuki.shopping.common.security;

import com.yuki.shopping.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** 业务代码获取当前登录用户的统一入口 */
public final class SecurityUtils {

    private SecurityUtils() { }

    public static LoginUser current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser user) {
            return user;
        }
        throw new BusinessException(40100, "未登录或Token无效");
    }

    public static Long currentUserId() {
        return current().userId();
    }
}
