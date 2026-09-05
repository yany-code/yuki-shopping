-- ============================================================================
-- 由岐商城 (Yuki Shopping) — 初版数据库建表脚本
-- 目标定位 : 类京东电商 Demo（技术学习 / 面试作品展示，仅系统级核心功能）
-- 适用版本 : MySQL 8.0+
-- 存储引擎 : InnoDB    字符集 : utf8mb4 / utf8mb4_general_ci
-- ----------------------------------------------------------------------------
-- 设计约定（面试讲解点）:
--   1. 金额字段统一使用 DECIMAL(10,2)，禁止 FLOAT/DOUBLE，避免精度丢失
--   2. 不建立物理外键：外键在高并发下带来额外行锁开销，且不利于后续
--      分库分表；关联完整性由应用层保证，关联字段一律建立索引
--   3. 通用字段：created_at / updated_at 全表必备；核心业务表带 deleted
--      逻辑删除标记，不做物理删除
--   4. 快照设计：订单保存下单时刻的商品价格/名称/规格、收货人信息快照，
--      不受商品后续改价、用户改地址影响
--   5. 防超卖：SKU 库存通过「条件更新 + 乐观锁版本号」扣减
--   6. 状态字段统一 TINYINT 枚举，取值间隔 10，便于后续插入中间状态
--   7. 表名统一 t_ 前缀；索引命名 pk_ / uk_ / idx_ 前缀
--
-- 模块范围：用户 / 商品 / 购物车 / 订单 / 支付 / 评价 / 后台管理
-- 暂不包含：优惠券等营销、秒杀、物流轨迹、售后逆向流程（后续版本扩展）
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `yuki_shopping`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE `yuki_shopping`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- 一、用户模块
-- ============================================================================

DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`      VARCHAR(50)  NOT NULL                COMMENT '登录用户名',
  `password_hash` VARCHAR(100) NOT NULL                COMMENT '密码哈希(BCrypt，不存明文)',
  `nickname`      VARCHAR(50)  NOT NULL                COMMENT '昵称',
  `phone`         VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
  `email`         VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
  `avatar`        VARCHAR(255) DEFAULT NULL            COMMENT '头像URL',
  `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 0-禁用 1-正常',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP      COMMENT '更新时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除 0-未删 1-已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

DROP TABLE IF EXISTS `t_user_address`;
CREATE TABLE `t_user_address` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id`        BIGINT UNSIGNED NOT NULL       COMMENT '所属用户ID',
  `receiver_name`  VARCHAR(50)  NOT NULL          COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20)  NOT NULL          COMMENT '收货人手机号',
  `province`       VARCHAR(50)  NOT NULL          COMMENT '省',
  `city`           VARCHAR(50)  NOT NULL          COMMENT '市',
  `district`       VARCHAR(50)  NOT NULL          COMMENT '区/县',
  `detail`         VARCHAR(200) NOT NULL          COMMENT '详细地址(街道门牌)',
  `is_default`     TINYINT      NOT NULL DEFAULT 0 COMMENT '默认地址 0-否 1-是',
  `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                   ON UPDATE CURRENT_TIMESTAMP    COMMENT '更新时间',
  `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删 1-已删',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户收货地址表';

-- ============================================================================
-- 二、商品模块（SPU / SKU 模型）
-- ============================================================================

DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id`  BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID，0=根节点',
  `name`       VARCHAR(50) NOT NULL           COMMENT '分类名称',
  `level`      TINYINT     NOT NULL           COMMENT '层级 1-一级 2-二级 3-三级',
  `sort`       INT         NOT NULL DEFAULT 0 COMMENT '同级排序，越小越靠前',
  `icon`       VARCHAR(255) DEFAULT NULL      COMMENT '分类图标URL',
  `status`     TINYINT     NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
              ON UPDATE CURRENT_TIMESTAMP    COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='商品分类表(三级类目树，parent_id 建树)';

DROP TABLE IF EXISTS `t_brand`;
CREATE TABLE `t_brand` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
  `name`        VARCHAR(50)  NOT NULL       COMMENT '品牌名称',
  `logo`        VARCHAR(255) DEFAULT NULL   COMMENT '品牌LOGO URL',
  `description` VARCHAR(255) DEFAULT NULL   COMMENT '品牌简介',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
               ON UPDATE CURRENT_TIMESTAMP  COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='品牌表';

DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID(SPU)',
  `category_id` BIGINT UNSIGNED NOT NULL       COMMENT '所属三级分类ID',
  `brand_id`    BIGINT UNSIGNED DEFAULT NULL   COMMENT '品牌ID',
  `name`        VARCHAR(100) NOT NULL          COMMENT '商品标题',
  `subtitle`    VARCHAR(200) DEFAULT NULL      COMMENT '副标题(卖点)',
  `main_image`  VARCHAR(255) DEFAULT NULL      COMMENT '主图URL(列表页展示)',
  `detail`      TEXT                           COMMENT '图文详情(富文本)',
  `price_min`   DECIMAL(10,2) DEFAULT NULL     COMMENT 'SKU最低价(冗余，列表页展示)',
  `price_max`   DECIMAL(10,2) DEFAULT NULL     COMMENT 'SKU最高价(冗余)',
  `sales`       INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计销量(冗余计数，异步累加)',
  `status`      TINYINT NOT NULL DEFAULT 0     COMMENT '状态 0-下架 1-上架',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
               ON UPDATE CURRENT_TIMESTAMP    COMMENT '更新时间',
  `deleted`     TINYINT NOT NULL DEFAULT 0     COMMENT '逻辑删除 0-未删 1-已删',
  PRIMARY KEY (`id`),
  KEY `idx_category_status` (`category_id`, `status`),
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_status_updated` (`status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='商品表(SPU，价格与库存挂靠SKU)';

DROP TABLE IF EXISTS `t_sku`;
CREATE TABLE `t_sku` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  `product_id`    BIGINT UNSIGNED NOT NULL      COMMENT '所属商品ID(SPU)',
  `sku_code`      VARCHAR(64) NOT NULL          COMMENT 'SKU编码(系统唯一)',
  `specs`         JSON DEFAULT NULL             COMMENT '规格组合，如 {"颜色":"黑","容量":"256G"}',
  `price`         DECIMAL(10,2) NOT NULL        COMMENT '销售价',
  `stock`         INT NOT NULL DEFAULT 0        COMMENT '可售库存',
  `stock_version` INT NOT NULL DEFAULT 0        COMMENT '库存乐观锁版本号',
  `image`         VARCHAR(255) DEFAULT NULL     COMMENT 'SKU图片URL',
  `status`        TINYINT NOT NULL DEFAULT 1    COMMENT '状态 0-禁用 1-启用',
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                 ON UPDATE CURRENT_TIMESTAMP   COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='SKU表(具体规格，交易/库存的最小单元)';

DROP TABLE IF EXISTS `t_product_image`;
CREATE TABLE `t_product_image` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '所属商品ID',
  `url`        VARCHAR(255) NOT NULL      COMMENT '图片URL',
  `sort`       INT NOT NULL DEFAULT 0     COMMENT '排序值，越小越靠前',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品相册表';

-- ============================================================================
-- 三、购物车模块
-- ============================================================================

DROP TABLE IF EXISTS `t_cart_item`;
CREATE TABLE `t_cart_item` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '购物车条目ID',
  `user_id`    BIGINT UNSIGNED NOT NULL     COMMENT '用户ID',
  `sku_id`     BIGINT UNSIGNED NOT NULL     COMMENT 'SKU ID',
  `quantity`   INT NOT NULL DEFAULT 1       COMMENT '数量',
  `checked`    TINYINT NOT NULL DEFAULT 1   COMMENT '结算勾选 0-否 1-是',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
              ON UPDATE CURRENT_TIMESTAMP  COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_sku` (`user_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='购物车表(同一用户同一SKU仅一条记录，重复加购累加数量)';

-- ============================================================================
-- 四、订单模块
-- ============================================================================

DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no`         VARCHAR(32) NOT NULL   COMMENT '订单号(业务唯一，应用层生成)',
  `user_id`          BIGINT UNSIGNED NOT NULL COMMENT '下单用户ID',
  -- 金额
  `total_amount`     DECIMAL(10,2) NOT NULL COMMENT '商品总金额',
  `freight_amount`   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费',
  `discount_amount`  DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额(预留营销扩展)',
  `pay_amount`       DECIMAL(10,2) NOT NULL COMMENT '实付金额 = 总金额 + 运费 - 优惠',
  -- 状态机: 10待支付 -> 20已支付(待发货) -> 30已发货(待收货) -> 40已完成
  --         10可转50已取消；售后整单退款转60
  `status`           TINYINT NOT NULL DEFAULT 10 COMMENT '订单状态 10-待支付 20-已支付待发货 30-已发货 40-已完成 50-已取消 60-已退款',
  -- 收货信息快照(下单时刻，不随用户地址修改变化)
  `receiver_name`    VARCHAR(50)  NOT NULL COMMENT '收货人姓名(快照)',
  `receiver_phone`   VARCHAR(20)  NOT NULL COMMENT '收货人手机号(快照)',
  `receiver_address` VARCHAR(300) NOT NULL COMMENT '完整收货地址(快照)',
  `remark`           VARCHAR(200) DEFAULT NULL COMMENT '买家备注',
  -- 物流
  `express_company`  VARCHAR(50)  DEFAULT NULL COMMENT '快递公司',
  `express_no`       VARCHAR(64)  DEFAULT NULL COMMENT '快递单号',
  -- 关键时间点
  `pay_time`         DATETIME DEFAULT NULL COMMENT '支付时间',
  `delivery_time`    DATETIME DEFAULT NULL COMMENT '发货时间',
  `finish_time`      DATETIME DEFAULT NULL COMMENT '完成(确认收货)时间',
  `cancel_time`      DATETIME DEFAULT NULL COMMENT '取消时间',
  `created_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `updated_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ON UPDATE CURRENT_TIMESTAMP      COMMENT '更新时间',
  `deleted`          TINYINT NOT NULL DEFAULT 0      COMMENT '逻辑删除 0-未删 1-已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_status_created` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='订单主表';

DROP TABLE IF EXISTS `t_order_item`;
CREATE TABLE `t_order_item` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id`     BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `user_id`      BIGINT UNSIGNED NOT NULL COMMENT '用户ID(冗余，便于「我的订单内商品」查询)',
  `product_id`   BIGINT UNSIGNED NOT NULL COMMENT '商品ID(SPU)',
  `sku_id`       BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  -- 下单时刻快照：商品改名/改价/删除均不影响历史订单展示
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品标题(快照)',
  `sku_specs`    VARCHAR(255) DEFAULT NULL COMMENT '规格描述(快照)，如 颜色:黑;容量:256G',
  `image`        VARCHAR(255) DEFAULT NULL COMMENT '图片URL(快照)',
  `price`        DECIMAL(10,2) NOT NULL   COMMENT '成交单价(快照)',
  `quantity`     INT NOT NULL             COMMENT '购买数量',
  `subtotal`     DECIMAL(10,2) NOT NULL   COMMENT '小计 = 单价 × 数量',
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='订单明细表(创建后不可变，仅保留created_at)';

-- ============================================================================
-- 五、支付模块
-- ============================================================================

DROP TABLE IF EXISTS `t_payment`;
CREATE TABLE `t_payment` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '支付ID',
  `payment_no` VARCHAR(32) NOT NULL   COMMENT '支付流水号(系统生成，全局唯一)',
  `order_id`   BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no`   VARCHAR(32) NOT NULL   COMMENT '订单号(冗余)',
  `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `pay_type`   TINYINT NOT NULL DEFAULT 1 COMMENT '支付方式 1-模拟支付(Demo) 2-支付宝 3-微信',
  `amount`     DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `status`     TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态 0-处理中 1-成功 2-失败',
  `trade_no`   VARCHAR(64) DEFAULT NULL COMMENT '第三方支付交易号(回调回填)',
  `paid_at`    DATETIME DEFAULT NULL COMMENT '支付完成时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
              ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='支付流水表(一次订单支付一条流水，支持重试场景多次记录)';

-- ============================================================================
-- 六、评价模块
-- ============================================================================

DROP TABLE IF EXISTS `t_review`;
CREATE TABLE `t_review` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `user_id`       BIGINT UNSIGNED NOT NULL COMMENT '评价用户ID',
  `order_id`      BIGINT UNSIGNED NOT NULL COMMENT '订单ID(已购才能评价)',
  `order_item_id` BIGINT UNSIGNED NOT NULL COMMENT '订单明细ID(定位到具体SKU)',
  `product_id`    BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `sku_id`        BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `rating`        TINYINT NOT NULL        COMMENT '评分 1-5星',
  `content`       VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',
  `images`        JSON DEFAULT NULL      COMMENT '评价图片URL数组',
  `is_anonymous`  TINYINT NOT NULL DEFAULT 0 COMMENT '匿名评价 0-否 1-是',
  `status`        TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-隐藏(违规) 1-显示',
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_item_id` (`order_item_id`),
  KEY `idx_product_id` (`product_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='商品评价表(一个订单明细仅可评价一次，创建后不可变)';

-- ============================================================================
-- 七、后台管理模块
-- ============================================================================

DROP TABLE IF EXISTS `t_admin_user`;
CREATE TABLE `t_admin_user` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username`      VARCHAR(50)  NOT NULL       COMMENT '登录名',
  `password_hash` VARCHAR(100) NOT NULL       COMMENT '密码哈希(BCrypt)',
  `real_name`     VARCHAR(50)  DEFAULT NULL   COMMENT '姓名',
  `role`          TINYINT NOT NULL DEFAULT 1  COMMENT '角色 1-普通管理员 2-超级管理员',
  `status`        TINYINT NOT NULL DEFAULT 1  COMMENT '状态 0-禁用 1-正常',
  `last_login_at` DATETIME DEFAULT NULL       COMMENT '最后登录时间',
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                 ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='后台管理员表(商品上下架、订单发货、评价管理的操作主体)';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 常用查询路径对照（索引设计依据）:
--   首页/类目商品列表   t_product  idx_category_status + idx_status_updated
--   商品详情            t_product -> t_sku / t_product_image (idx_product_id)
--   购物车列表          t_cart_item (uk_user_sku)
--   我的订单/按状态筛选  t_order  idx_user_status
--   后台订单管理        t_order  idx_status_created
--   商品评价列表        t_review idx_product_id
-- ============================================================================
