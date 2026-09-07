package com.yuki.shopping.modules.user.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressDTO {

    @NotBlank(message = "receiverName 不能为空")
    @Size(max = 50,message = "receiver 长度不能超过50")
    private String receiverName;

    @NotBlank(message = "receiverPhone 不能为空")
    @Size(max =20,message = "receiverPhone 长度不能超过20")
    private String receiverPhone;

    @NotBlank(message = "province 不能为空")
    @Size(max = 50,message = "province 长度不能超过50")
    private String province;

    @NotBlank(message = "city 不能为空")
    @Size(max = 50,message = "city 长度不能超过50")
    private String city;

    @NotBlank(message = "district 不能为空")
    @Size(max = 50,message = "district 长度不能超过50")
    private String district;

    @NotBlank(message = "detail 不能为空")
    @Size(max = 200,message = "detail 长度不能超过200")
    private String detail;

    /**
     * 默认地址 0-否 1-是
     */
    @Min(value = 0 ,message = "isDefault 必须为0或1")
    @Max(value = 1 ,message = "isDefault 必须为0或1")
    private Integer isDefault;
}
