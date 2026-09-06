package com.yuki.shopping.modules.cart.domain;

import lombok.Data;

import java.math.BigDecimal;

/** 购物车条目，商品信息为加入时刻快照 */
@Data
public class CartItemVO {

    private Long id;

    private Long skuId;

    private Long productId;

    private String productName;

    private String skuSpecs;

    private String image;

    private BigDecimal price;

    private Integer quantity;

    /** 结算勾选 0-否 1-是 */
    private Integer checked;
}
