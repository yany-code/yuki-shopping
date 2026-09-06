package com.yuki.shopping.modules.cart.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemAddDTO {

    @NotNull
    private Long skuId;

    @NotNull
    @Min(1)
    private Integer quantity;

    /** 结算勾选 0-否 1-是 */
    private Integer checked;
}
