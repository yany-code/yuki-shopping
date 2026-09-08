package com.yuki.shopping.modules.product.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductQuery {

    private String keyword;

    private Long categoryId;

    private Long brandId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    /** default | price_asc | price_desc | sales | newest */
    private String sort = "default";

    private long page = 1;

    private long pageSize = 20;
}