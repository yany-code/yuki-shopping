package com.yuki.shopping.modules.product.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class SkuVO {

    private Long id;

    private String skuCode;

    /** 规格组合，如 {"颜色":"黑","容量":"256G"} */
    private Map<String, String> specs;

    private BigDecimal price;

    private Integer stock;

    private String image;
}
