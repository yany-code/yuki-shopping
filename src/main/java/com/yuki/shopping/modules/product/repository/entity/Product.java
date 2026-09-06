package com.yuki.shopping.modules.product.repository.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属三级分类ID */
    private Long categoryId;

    private Long brandId;

    private String name;

    private String subtitle;

    private String mainImage;

    /** 图文详情(富文本) */
    private String detail;

    /** SKU最低价(冗余，列表页展示) */
    private BigDecimal priceMin;

    /** SKU最高价(冗余) */
    private BigDecimal priceMax;

    /** 累计销量(冗余计数) */
    private Long sales;

    /** 0-下架 1-上架 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private Integer deleted;
}
