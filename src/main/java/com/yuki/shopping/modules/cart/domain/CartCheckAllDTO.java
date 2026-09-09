package com.yuki.shopping.modules.cart.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartCheckAllDTO {

    /** 全选状态 0-全不选 1-全选 */
    @NotNull
    @Min(0)
    @Max(1)
    private Integer checked;
}
