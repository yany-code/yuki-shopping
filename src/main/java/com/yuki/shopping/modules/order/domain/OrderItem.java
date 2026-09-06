package com.yuki.shopping.modules.order.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订单明细：创建后不可变，商品名称/规格/图片/价格均为下单时刻快照 */
@Data
@TableName("t_order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    /** 冗余用户ID，便于按用户查询订单内商品 */
    private Long userId;

    private Long productId;

    private Long skuId;

    /** 商品标题(快照) */
    private String productName;

    /** 规格描述(快照)，如 颜色:黑;容量:256G */
    private String skuSpecs;

    /** 图片URL(快照) */
    private String image;

    /** 成交单价(快照) */
    private BigDecimal price;

    private Integer quantity;

    /** 小计 = 单价 × 数量 */
    private BigDecimal subtotal;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
