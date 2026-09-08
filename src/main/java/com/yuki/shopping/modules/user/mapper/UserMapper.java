package com.yuki.shopping.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuki.shopping.modules.user.domain.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("""
        SELECT *
        FROM t_user
        WHERE id = #{userId}
        FOR UPDATE
        """)
    User selectByIdForUpdate(Long userId);

}
