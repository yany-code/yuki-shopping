package com.yuki.shopping.modules.product.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuVO {

    private Long id;

    private String skuCode;

    /** 规格组合 JSON，如 {"颜色":"黑","容量":"256G"} */
    private String specs;

    private BigDecimal price;

    private Integer stock;

    private String image;
}
