package com.yuki.shopping.modules.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuki.shopping.modules.cart.domain.CartItem;
import org.apache.ibatis.annotations.Param;

public interface CartItemMapper extends BaseMapper<CartItem> {

    /** 加购原子 upsert：用户+SKU 不存在则插入，已存在则数量累加并恢复勾选；返回 1=插入 2=更新 */
    int upsertAdd(@Param("userId") Long userId,
                  @Param("skuId") Long skuId,
                  @Param("quantity") Integer quantity,
                  @Param("checked") Integer checked);
}
