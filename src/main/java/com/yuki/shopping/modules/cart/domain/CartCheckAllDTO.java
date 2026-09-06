package com.yuki.shopping.modules.cart.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartCheckAllDTO {

    /** 全选状态 0-全不选 1-全选 */
    @NotNull
    private Integer checked;
}
