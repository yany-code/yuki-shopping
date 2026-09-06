package com.yuki.shopping.modules.product.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewVO {

    private Long id;

    private Long userId;

    /** 评价人昵称(匿名时脱敏) */
    private String nickname;

    private Long productId;

    private Long skuId;

    /** 评分 1-5星 */
    private Integer rating;

    private String content;

    /** 评价图片URL */
    private List<String> images;

    /** 匿名评价 0-否 1-是 */
    private Integer isAnonymous;

    /** 0-隐藏 1-显示 */
    private Integer status;

    private LocalDateTime createdAt;
}
