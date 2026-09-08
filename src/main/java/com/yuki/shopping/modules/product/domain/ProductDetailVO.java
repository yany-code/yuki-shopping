package com.yuki.shopping.modules.product.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetailVO {

    private Long id;

    /** 所属三级分类ID */
    private Long categoryId;

    private Long brandId;

    private String name;

    private String subtitle;

    private String mainImage;

    /** 图文详情(富文本) */
    private String detail;

    /** SKU最低价(冗余) */
    private BigDecimal priceMin;

    /** SKU最高价(冗余) */
    private BigDecimal priceMax;

    /** 累计销量(冗余计数) */
    private Long sales;

    /** SKU列表 */
    private List<SkuVO> skus;

    /** 商品图集 */
    private List<String> images;

    /**
     * 评价摘要(均分/数量)
     */
    private ReviewSummaryVO reviewSummary;
}
