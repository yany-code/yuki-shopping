package com.yuki.shopping.modules.product.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewSummaryVO {

    /** 平均评分，保留 1 位小数 */
    private BigDecimal avgRating;

    /** 可见评价数量 */
    private Long reviewCount;
}