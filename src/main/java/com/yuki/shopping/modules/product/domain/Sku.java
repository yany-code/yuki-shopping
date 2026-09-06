package com.yuki.shopping.modules.product.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_sku")
public class Sku {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String skuCode;

    /** 规格组合 JSON，如 {"颜色":"黑","容量":"256G"} */
    private String specs;

    private BigDecimal price;

    /** 可售库存 */
    private Integer stock;

    /** 库存乐观锁版本号，条件扣库存时 stock = stock - n 且 stock_version + 1 */
    @Version
    private Integer stockVersion;

    private String image;

    /** 0-禁用 1-启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
