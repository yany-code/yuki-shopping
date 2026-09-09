package com.yuki.shopping.modules.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuki.shopping.modules.product.domain.Sku;
import org.apache.ibatis.annotations.Param;

public interface SkuMapper extends BaseMapper<Sku> {

    /** 条件扣库存：stock >= quantity 时才扣减并推进版本号，返回受影响行数（0 = 库存不足或并发冲突） */
    int deductStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    /** 取消/超时释放库存：无条件加回（只增不减无冲突面，不动版本号） */
    int restoreStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);
}
