package com.yuki.shopping.modules.cart.domain;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CartItemUpdateDTO {

    @Min(1)
    private Integer quantity;

    /** 结算勾选 0-否 1-是 */
    private Integer checked;
}
