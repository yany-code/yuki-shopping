package com.yuki.shopping.modules.product.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductListVO {

    private Long id;

    private String name;

    private String subtitle;

    private String mainImage;

    /** SKU最低价(冗余，列表页展示) */
    private BigDecimal priceMin;

    /** SKU最高价(冗余) */
    private BigDecimal priceMax;

    /** 累计销量(冗余计数) */
    private Long sales;
}
