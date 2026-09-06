package com.yuki.shopping.modules.admin.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderShipDTO {

    @NotBlank
    private String expressCompany;

    @NotBlank
    private String expressNo;
}
