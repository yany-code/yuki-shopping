package com.yuki.shopping.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuki.shopping.common.exception.BusinessException;
import com.yuki.shopping.common.security.SecurityUtils;
import com.yuki.shopping.modules.order.domain.Order;
import com.yuki.shopping.modules.order.domain.OrderItem;
import com.yuki.shopping.modules.order.domain.ReviewCreatedVO;
import com.yuki.shopping.modules.order.mapper.OrderItemMapper;
import com.yuki.shopping.modules.order.mapper.OrderMapper;
import com.yuki.shopping.modules.order.service.ReviewService;
import com.yuki.shopping.modules.product.domain.Review;
import com.yuki.shopping.modules.product.domain.ReviewDTO;
import com.yuki.shopping.modules.product.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评价业务放在 order 模块：编排依赖订单域（归属 + 状态 + 明细校验），
 * 若放 product 则形成 product → order 反向依赖，违反模块依赖约定
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ReviewMapper reviewMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ReviewCreatedVO review(String orderNo, Long itemId, ReviewDTO request) {
        // 订单归属：查不到 40400，不属于当前用户 40300（全项目统一约定）
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(40400, "订单不存在");
        }
        if (!order.getUserId().equals(SecurityUtils.currentUserId())) {
            throw new BusinessException(40300, "无权限操作该订单");
        }
        // 前置状态校验（非状态流转，不走状态机）：仅已完成订单可评价
        if (order.getStatus() != Order.STATUS_FINISHED) {
            throw new BusinessException(40900, "订单完成后才能评价");
        }
        // 明细归属：必须属于该订单
        OrderItem item = orderItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(40400, "订单明细不存在");
        }
        if (!item.getOrderId().equals(order.getId())) {
            throw new BusinessException(40300, "明细不属于该订单");
        }
        // 查重给友好提示；不筛 status：被管理端隐藏的评价仍占用「一明细一评」名额。
        // 并发重复由 uk_order_item_id 唯一键兜底（DuplicateKeyException 转业务码）
        Long reviewed = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderItemId, itemId));
        if (reviewed > 0) {
            throw new BusinessException(40900, "该商品明细已评价过");
        }

        Review review = new Review();
        review.setUserId(order.getUserId());
        review.setOrderId(order.getId());
        review.setOrderItemId(item.getId());
        // 锚定成交快照：productId/skuId 从订单明细取，不信任客户端
        review.setProductId(item.getProductId());
        review.setSkuId(item.getSkuId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setImages(serializeImages(request.getImages()));
        review.setIsAnonymous(Integer.valueOf(1).equals(request.getIsAnonymous()) ? 1 : 0);
        review.setStatus(1); // 默认显示；违规隐藏由管理端处理（阶段 6）
        try {
            reviewMapper.insert(review);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(40900, "该商品明细已评价过");
        }
        ReviewCreatedVO vo = new ReviewCreatedVO();
        vo.setReviewId(review.getId());
        return vo;
    }

    /** images 序列化为 JSON 入库，与商品侧评价列表的 parseImages 互为镜像 */
    private String serializeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
