package com.yuki.shopping.modules.user.domain;

import lombok.Data;

@Data
public class AddressVO {

    private Long id;

    private String receiverName;

    private String receiverPhone;

    private String province;

    private String city;

    private String district;

    private String detail;

    /** 默认地址 0-否 1-是 */
    private Integer isDefault;
}
