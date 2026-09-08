package com.yuki.shopping.modules.product.domain;

import lombok.Data;

@Data
public class BrandVO {

    private Long id;

    private String name;

    private String logo;

    private String description;
}