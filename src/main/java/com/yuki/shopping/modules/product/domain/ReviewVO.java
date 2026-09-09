package com.yuki.shopping.modules.product.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewVO {

    private Long id;

    /** 评价人ID，匿名评价为 null（配合全局 non_null 序列化后字段不出现，防追溯） */
    private Long userId;

    /** 评价人昵称(匿名时后端覆盖为脱敏值) */
    private String nickname;

    /** 评价人头像URL(匿名时为 null) */
    private String avatar;

    private Long productId;

    private Long skuId;

    /** 评分 1-5星 */
    private Integer rating;

    private String content;

    /** 评价图片URL */
    private List<String> images;

    /** 匿名评价 0-否 1-是 */
    private Integer isAnonymous;

    private LocalDateTime createdAt;
}
