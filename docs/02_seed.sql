-- ============================================================================
-- 由岐商城 — 演示数据种子脚本（在 01_schema.sql 之后执行）
-- 说明：
--   1. 依赖自增 ID 顺序，请在空库按 01 -> 02 顺序执行，不要重复执行
--   2. 账号密码均为 BCrypt 哈希：
--        admin / admin123      超级管理员(role=2)
--        ops   / admin123      普通管理员(role=1)
--        yuki  / yuki123456    客户端用户
--        demo  / demo123456    客户端用户
--   3. SKU 显式指定 1001 起始 ID，与接口文档示例中的 skuId 保持一致
-- ============================================================================

USE `yuki_shopping`;

-- ----------------------------------------------------------------------------
-- 管理员与用户
-- ----------------------------------------------------------------------------
INSERT INTO `t_admin_user` (`username`, `password_hash`, `real_name`, `role`, `status`) VALUES
('admin', '$2a$10$S9jb3iBIe0MtsX8bjtGVl.kYgKBSN48qiKrZRcswDThV5mUkVXmfG', '超级管理员', 2, 1),
('ops',   '$2a$10$S9jb3iBIe0MtsX8bjtGVl.kYgKBSN48qiKrZRcswDThV5mUkVXmfG', '运营管理员', 1, 1);

INSERT INTO `t_user` (`username`, `password_hash`, `nickname`, `phone`, `email`, `status`) VALUES
('yuki', '$2a$10$MmnVoB/1B9clll0hfL.7aeTBzi0Cd2i.TimT5rx8B7Zi4H.U7vky2', '由岐', '13800000001', 'yuki@example.com', 1),
('demo', '$2a$10$erM3f9kGRKBrnXiJxZSiqOHKN584uUuDsVAxlXqZmqq/yGajzIeJu', '演示用户', '13800000002', NULL, 1);

-- id 1 -> user 1 默认地址；id 2 -> user 1 备用；id 3 -> user 2 默认
INSERT INTO `t_user_address` (`user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail`, `is_default`) VALUES
(1, '由岐', '13800000001', '上海市', '上海市', '浦东新区', '世纪大道100号环球金融中心 32F', 1),
(1, '由岐', '13800000001', '浙江省', '杭州市', '西湖区', '文三路 100 号 5 幢 201', 0),
(2, '演示用户', '13800000002', '广东省', '深圳市', '南山区', '科技园南区 8 栋 1201', 1);

-- ----------------------------------------------------------------------------
-- 分类：一级(1~3) / 二级(4~8) / 三级(9~13)
-- ----------------------------------------------------------------------------
INSERT INTO `t_category` (`parent_id`, `name`, `level`, `sort`) VALUES
(0, '数码电器', 1, 1),
(0, '服饰鞋包', 1, 2),
(0, '食品生鲜', 1, 3);

INSERT INTO `t_category` (`parent_id`, `name`, `level`, `sort`) VALUES
(1, '手机通讯', 2, 1),
(1, '电脑办公', 2, 2),
(2, '男装',     2, 1),
(2, '女装',     2, 2),
(3, '休闲零食', 2, 1);

INSERT INTO `t_category` (`parent_id`, `name`, `level`, `sort`) VALUES
(4, '智能手机',   3, 1),
(5, '笔记本电脑', 3, 1),
(6, 'T恤',        3, 1),
(7, '连衣裙',     3, 2),
(8, '坚果炒货',   3, 1);

-- ----------------------------------------------------------------------------
-- 品牌
-- ----------------------------------------------------------------------------
INSERT INTO `t_brand` (`name`, `logo`, `description`) VALUES
('Apple',     'https://cdn.example.com/brand/apple.png',     '苹果公司'),
('华为',      'https://cdn.example.com/brand/huawei.png',    '构建万物互联的智能世界'),
('小米',      'https://cdn.example.com/brand/xiaomi.png',    '让每个人都能享受科技的乐趣'),
('优衣库',    'https://cdn.example.com/brand/uniqlo.png',    'LifeWear 服适人生'),
('三只松鼠',  'https://cdn.example.com/brand/songshu.png',   '主人，来点坚果吗');

-- ----------------------------------------------------------------------------
-- 商品 SPU（1~5，全部上架）
-- ----------------------------------------------------------------------------
INSERT INTO `t_product` (`id`, `category_id`, `brand_id`, `name`, `subtitle`, `main_image`, `detail`, `price_min`, `price_max`, `sales`, `status`) VALUES
(1, 9,  1, 'Apple iPhone 15 Pro 256GB', 'A17 Pro 芯片 · 钛金属设计', 'https://cdn.example.com/product/iphone15pro/main.jpg', '<p>A17 Pro 芯片，钛金属边框，4800 万像素主摄。</p>', 8999.00, 9299.00, 1200, 1),
(2, 9,  2, '华为 Mate 60 Pro',          '卫星通话 · 鸿蒙系统',      'https://cdn.example.com/product/mate60pro/main.jpg',   '<p>双卫星通信，玄武架构，超光变摄像头。</p>',       6999.00, 6999.00, 800,  1),
(3, 10, 3, '小米笔记本 Pro 14',         '2.8K 超清屏 · 铝合金机身', 'https://cdn.example.com/product/mibookpro/main.jpg',   '<p>2.8K 120Hz OLED 大师屏，标压处理器。</p>',       4999.00, 5999.00, 300,  1),
(4, 11, 4, '优衣库 圆领T恤(短袖)',      '柔软棉质 · 百搭基础款',    'https://cdn.example.com/product/tshirt/main.jpg',      '<p>精选棉料，亲肤透气。</p>',                       79.00,   79.00,   5000, 1),
(5, 13, 5, '三只松鼠 每日坚果 750g',    '30包混合装 · 科学配比',    'https://cdn.example.com/product/nuts/main.jpg',        '<p>六种坚果果干，每日一包。</p>',                   89.90,   89.90,   20000, 1);

-- ----------------------------------------------------------------------------
-- SKU（显式 ID 1001+，与接口文档示例一致）
-- ----------------------------------------------------------------------------
INSERT INTO `t_sku` (`id`, `product_id`, `sku_code`, `specs`, `price`, `stock`, `stock_version`, `image`, `status`) VALUES
(1001, 1, 'SKU-IP15P-256-ORIG', '{"颜色":"原色钛金属","容量":"256GB"}', 8999.00, 50,  0, 'https://cdn.example.com/product/iphone15pro/orig.jpg',   1),
(1002, 1, 'SKU-IP15P-256-BLCK', '{"颜色":"黑色钛金属","容量":"256GB"}', 8999.00, 50,  0, 'https://cdn.example.com/product/iphone15pro/black.jpg',  1),
(1003, 1, 'SKU-IP15P-256-WHIT', '{"颜色":"白色钛金属","容量":"256GB"}', 9299.00, 30,  0, 'https://cdn.example.com/product/iphone15pro/white.jpg',  1),
(1004, 2, 'SKU-MATE60P-512-BK', '{"颜色":"雅丹黑","内存":"12GB+512GB"}', 6999.00, 40, 0, 'https://cdn.example.com/product/mate60pro/black.jpg',    1),
(1005, 2, 'SKU-MATE60P-512-SL', '{"颜色":"白沙银","内存":"12GB+512GB"}', 6999.00, 40, 0, 'https://cdn.example.com/product/mate60pro/silver.jpg',   1),
(1006, 3, 'SKU-MIBOOK-14-STD',  '{"内存":"16GB+512GB"}',               4999.00, 60,  0, 'https://cdn.example.com/product/mibookpro/std.jpg',      1),
(1007, 3, 'SKU-MIBOOK-14-HGH',  '{"内存":"32GB+1TB"}',                 5999.00, 30,  0, 'https://cdn.example.com/product/mibookpro/high.jpg',     1),
(1008, 4, 'SKU-TSHIRT-M-WHITE', '{"颜色":"白色","尺码":"M"}',           79.00,   500, 0, 'https://cdn.example.com/product/tshirt/white.jpg',       1),
(1009, 4, 'SKU-TSHIRT-L-BLACK', '{"颜色":"黑色","尺码":"L"}',           79.00,   500, 0, 'https://cdn.example.com/product/tshirt/black.jpg',       1),
(1010, 4, 'SKU-TSHIRT-XL-NVY',  '{"颜色":"藏青","尺码":"XL"}',          79.00,   300, 0, 'https://cdn.example.com/product/tshirt/navy.jpg',        1),
(1011, 5, 'SKU-NUTS-750G-30',   '{"规格":"750g/30包"}',                 89.90,   200, 0, 'https://cdn.example.com/product/nuts/750g.jpg',          1);

-- ----------------------------------------------------------------------------
-- 商品相册
-- ----------------------------------------------------------------------------
INSERT INTO `t_product_image` (`product_id`, `url`, `sort`) VALUES
(1, 'https://cdn.example.com/product/iphone15pro/1.jpg', 1),
(1, 'https://cdn.example.com/product/iphone15pro/2.jpg', 2),
(1, 'https://cdn.example.com/product/iphone15pro/3.jpg', 3),
(2, 'https://cdn.example.com/product/mate60pro/1.jpg',   1),
(2, 'https://cdn.example.com/product/mate60pro/2.jpg',   2),
(3, 'https://cdn.example.com/product/mibookpro/1.jpg',   1),
(4, 'https://cdn.example.com/product/tshirt/1.jpg',      1),
(4, 'https://cdn.example.com/product/tshirt/2.jpg',      2),
(5, 'https://cdn.example.com/product/nuts/1.jpg',        1);
