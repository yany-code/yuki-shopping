package com.yuki.shopping.modules.product.repository.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long orderId;

    /** 订单明细ID，唯一约束 uk_order_item_id：一条明细仅可评价一次 */
    private Long orderItemId;

    private Long productId;

    private Long skuId;

    /** 评分 1-5星 */
    private Integer rating;

    private String content;

    /** 评价图片URL数组 JSON */
    private String images;

    /** 匿名评价 0-否 1-是 */
    private Integer isAnonymous;

    /** 0-隐藏(违规) 1-显示 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
