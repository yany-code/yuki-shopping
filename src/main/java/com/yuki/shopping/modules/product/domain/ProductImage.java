package com.yuki.shopping.modules.product.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_product_image")
public class ProductImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String url;

    /** 排序值，越小越靠前 */
    private Integer sort;
}
