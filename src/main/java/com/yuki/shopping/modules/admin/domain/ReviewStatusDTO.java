package com.yuki.shopping.modules.admin.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewStatusDTO {

    /** 0-隐藏(违规) 1-显示 */
    @NotNull
    private Integer status;
}
