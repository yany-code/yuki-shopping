package com.yuki.shopping.modules.order.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {

    @NotNull
    private Long addressId;

    @NotEmpty
    @Valid
    private List<OrderItemDTO> items;

    @Size(max = 200)
    private String remark;
}
