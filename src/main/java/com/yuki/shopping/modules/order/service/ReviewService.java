package com.yuki.shopping.modules.order.service;

import com.yuki.shopping.modules.order.domain.ReviewCreatedVO;
import com.yuki.shopping.modules.product.domain.ReviewDTO;

public interface ReviewService {

    /** 已完成订单的明细评价，一条明细仅可评价一次（uk_order_item_id 兜底） */
    ReviewCreatedVO review(String orderNo, Long itemId, ReviewDTO request);
}
