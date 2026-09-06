package com.yuki.shopping.modules.product.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ReviewDTO {

    /** 评分 1-5星 */
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 1000)
    private String content;

    /** 评价图片URL */
    private List<String> images;

    /** 匿名评价 0-否 1-是 */
    private Integer isAnonymous;
}
