package com.yuki.shopping.modules.order.domain;

import lombok.Data;

/** 评价提交结果：返回 reviewId 供前端定位新评价，省一次列表拉取 */
@Data
public class ReviewCreatedVO {

    private Long reviewId;
}
