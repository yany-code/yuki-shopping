package com.yuki.shopping.modules.cart.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/** 购物车条目，商品信息与价格为实时读取（结算时还会以服务端实时数据为准） */
@Data
public class CartItemVO {

    private Long id;

    private Long skuId;

    private Long productId;

    private String productName;

    /** 规格组合，与详情接口 SkuVO.specs 同为结构化对象 */
    private Map<String, String> skuSpecs;

    private String image;

    private BigDecimal price;

    private Integer quantity;

    /** 结算勾选 0-否 1-是 */
    private Integer checked;

    /** 可售状态：SKU 启用且商品上架；下架/禁用时为 false，前端置灰 */
    private Boolean onSale;

    /** 库存是否充足：quantity 超过实时库存时为 false */
    private Boolean stockEnough;
}
