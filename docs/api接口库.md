# 由岐商城接口库（V1）

## 1. 文档说明

本文档依据 `sql/01_schema.sql` 设计，面向学习项目的前后端联调和接口实现。接口前缀为 `/api/v1`，数据格式为 `application/json`，字符集为 UTF-8。金额统一使用字符串传输（例如 `"99.90"`），避免 JavaScript 浮点误差；时间使用 `yyyy-MM-dd HH:mm:ss`。

接口分为客户端（用户 Bearer Token）和管理端（管理员 Bearer Token）。除登录、注册、商品浏览、分类和品牌查询外，其他接口均需鉴权。

## 2. 通用协议

### 2.1 请求头

```http
Authorization: Bearer <access_token>
Content-Type: application/json
Idempotency-Key: <客户端生成的唯一值>   # 创建订单、发起支付建议携带
```

### 2.2 统一响应

```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "traceId": "01J..."
}
```

`code=0` 表示成功。失败时 `data` 为 `null`，业务错误码建议如下：`40000` 参数错误、`40100` 未登录/Token 无效、`40300` 无权限、`40400` 资源不存在、`40500` 请求方法不支持、`40900` 状态冲突或库存不足、`42900` 请求过于频繁、`50000` 系统异常。除 `40500` 伴随 HTTP 状态 405 返回外，其余业务错误一律 HTTP 200 + 统一响应体。分页统一返回：

```json
{"list": [], "page": 1, "pageSize": 20, "total": 0}
```

### 2.3 数据与安全约定

- 密码只保存 BCrypt 哈希；登录成功返回短期 access token 和 refresh token，服务端不得返回 `password_hash`。
- 所有资源按当前用户 `user_id` 做数据隔离；客户端传入的 `userId`、价格、库存、订单金额均不可信。
- 商品详情读取上架商品和启用 SKU；订单创建时重新查询价格、状态和库存，并保存商品、规格、图片、收货地址快照。
- 扣库存使用条件更新：`stock >= quantity` 且 `stock_version` 匹配时 `stock = stock - quantity, stock_version = stock_version + 1`；任一 SKU 失败则整单回滚。
- 创建订单、支付创建和支付回调必须幂等；重复请求返回第一次成功结果。

## 3. 客户端接口

### 3.1 用户与地址

| 方法 | 路径 | 说明 | 主要表 |
|---|---|---|---|
| POST | `/auth/register` | 注册用户名、密码、昵称、手机号、邮箱 | `t_user` |
| POST | `/auth/login` | 用户名密码登录 | `t_user` |
| POST | `/auth/refresh` | 刷新 access token | Token 存储 |
| GET | `/users/me` | 当前用户资料 | `t_user` |
| PUT | `/users/me` | 修改昵称、头像、邮箱、手机号 | `t_user` |
| GET | `/users/me/addresses` | 地址列表，按默认地址优先 | `t_user_address` |
| POST | `/users/me/addresses` | 新增地址；`isDefault=1` 时清除旧默认 | `t_user_address` |
| PUT | `/users/me/addresses/{id}` | 修改本人地址 | `t_user_address` |
| DELETE | `/users/me/addresses/{id}` | 逻辑删除地址 | `t_user_address` |
| PUT | `/users/me/addresses/{id}/default` | 设置默认地址 | `t_user_address` |

注册请求示例：

```json
{"username":"yuki","password":"P@ssw0rd1","nickname":"由岐","phone":"13800000000"}
```

地址字段：`receiverName`、`receiverPhone`、`province`、`city`、`district`、`detail`、`isDefault`。地址删除或修改必须校验归属；下单后只读取快照，不回查地址。

### 3.2 分类、品牌与商品

| 方法 | 路径 | 说明 | 主要表 |
|---|---|---|---|
| GET | `/categories/tree` | 返回启用的三级分类树 | `t_category` |
| GET | `/brands` | 品牌分页查询 | `t_brand` |
| GET | `/products` | 商品分页、关键词、分类、品牌、价格区间、排序查询 | `t_product`、`t_sku` |
| GET | `/products/{productId}` | 商品详情、SKU、相册、评价摘要 | `t_product`、`t_sku`、`t_product_image`、`t_review` |
| GET | `/products/{productId}/reviews` | 评价分页，可按星级筛选 | `t_review` |

`GET /products` 参数：`keyword`、`categoryId`、`brandId`、`minPrice`、`maxPrice`、`sort`（`default|price_asc|price_desc|sales|newest`）、`page`、`pageSize`。商品列表只返回上架且未删除数据，价格取 `priceMin/priceMax`。参数行为约定：`categoryId` 传任意层级均展开为其自身与全部后代分类的商品（分类不存在返回 `40400`）；价格区间按「区间重叠即命中」过滤，即商品价区 `[priceMin, priceMax]` 与查询区间 `[minPrice, maxPrice]` 有交集即返回；`sort` 非法值返回 `40000`；`page` 必须 ≥ 1、`pageSize` 取值 1~100、价格不得为负且 `minPrice ≤ maxPrice`，违反返回 `40000` 并带字段说明（服务端不静默改写）。`GET /products/{productId}/reviews` 同样受分页与 `rating`（1~5）约束。

### 3.3 购物车

| 方法 | 路径 | 说明 | 主要表 |
|---|---|---|---|
| GET | `/cart` | 当前用户购物车，实时返回 SKU 可售状态 | `t_cart_item`、`t_sku`、`t_product` |
| POST | `/cart/items` | 加入购物车；同 SKU 累加数量 | `t_cart_item` |
| PUT | `/cart/items/{id}` | 修改数量或勾选状态 | `t_cart_item` |
| DELETE | `/cart/items/{id}` | 删除购物车条目 | `t_cart_item` |
| PUT | `/cart/items/check-all` | 批量设置勾选状态 | `t_cart_item` |

加入请求：`{"skuId":1001,"quantity":2}`；数量必须大于 0，最终库存以创建订单时校验为准。

### 3.4 订单与支付

| 方法 | 路径 | 说明 | 主要表 |
|---|---|---|---|
| POST | `/orders/preview` | 根据地址和勾选购物车试算金额，不扣库存 | 读取购物车、SKU |
| POST | `/orders` | 创建订单、锁定/扣减库存、写入快照 | `t_order`、`t_order_item`、`t_cart_item`、`t_sku` |
| GET | `/orders` | 我的订单分页，可按状态筛选 | `t_order` |
| GET | `/orders/{orderNo}` | 订单详情、明细和支付信息 | `t_order`、`t_order_item`、`t_payment` |
| POST | `/orders/{orderNo}/cancel` | 取消待支付订单并释放库存 | `t_order`、`t_sku` |
| POST | `/orders/{orderNo}/confirm` | 确认收货，状态 30→40 | `t_order` |
| POST | `/orders/{orderNo}/pay` | 创建支付流水并返回模拟支付参数 | `t_payment`、`t_order` |
| POST | `/payments/callback` | 支付渠道回调（验签后处理） | `t_payment`、`t_order` |

创建订单请求：

```json
{
  "addressId": 12,
  "items": [{"skuId": 1001, "quantity": 2}],
  "remark": "工作日送货"
}
```

服务端事务：校验地址归属 → 查询 SKU 和商品 → 校验上架状态 → 条件扣库存 → 计算金额 → 写入订单及明细快照 → 删除/取消已结算购物车条目。返回 `orderNo`、金额、状态和明细。金额公式：`payAmount = totalAmount + freightAmount - discountAmount`。

订单状态：`10 待支付`、`20 已支付待发货`、`30 已发货`、`40 已完成`、`50 已取消`、`60 已退款`。允许流转：`10→20`（支付成功）、`10→50`（用户取消或超时取消）、`20→30`（后台发货）、`30→40`（确认收货）、`20/30→60`（后续退款扩展）。非法状态转换返回 `40900`。

支付创建请求：`{"payType":1}`，仅允许订单状态 10，金额从订单读取；支付回调必须按 `paymentNo`、签名和金额校验，重复成功回调直接返回成功，不重复推进订单。

### 3.5 评价

| 方法 | 路径 | 说明 | 主要表 |
|---|---|---|---|
| POST | `/orders/{orderNo}/items/{itemId}/review` | 已完成订单的明细评价，一条明细一次 | `t_review` |

请求字段：`rating`（1~5）、`content`、`images`（URL 数组）、`isAnonymous`。服务端校验订单归属、状态 40、明细归属和未评价唯一约束 `uk_order_item_id`。

## 4. 管理端接口

管理端请求头使用管理员 Token。普通管理员可执行商品、订单和评价操作；超级管理员额外管理管理员账号。

| 方法 | 路径 | 权限 | 说明 | 主要表 |
|---|---|---|---|---|
| POST | `/admin/auth/login` | 公开 | 管理员登录 | `t_admin_user` |
| GET | `/admin/products` | 管理员 | 商品分页与状态筛选 | `t_product` |
| POST | `/admin/products` | 管理员 | 创建 SPU、SKU、相册 | 多表 |
| PUT | `/admin/products/{id}` | 管理员 | 修改商品资料 | `t_product` |
| PUT | `/admin/products/{id}/status` | 管理员 | 上架/下架 | `t_product` |
| POST | `/admin/products/{id}/skus` | 管理员 | 新增 SKU | `t_sku` |
| PUT | `/admin/skus/{skuId}` | 管理员 | 修改价格、库存、规格状态 | `t_sku` |
| GET | `/admin/orders` | 管理员 | 按状态、订单号、时间分页 | `t_order` |
| GET | `/admin/orders/{orderNo}` | 管理员 | 查看订单详情 | `t_order`、`t_order_item` |
| POST | `/admin/orders/{orderNo}/ship` | 管理员 | 发货，10/20 状态校验 | `t_order` |
| GET | `/admin/reviews` | 管理员 | 评价审核 | `t_review` |
| PUT | `/admin/reviews/{id}/status` | 管理员 | 隐藏/显示评价 | `t_review` |
| GET | `/admin/categories` | 管理员 | 分类树管理 | `t_category` |
| POST/PUT | `/admin/categories`、`/admin/categories/{id}` | 管理员 | 新增、编辑分类 | `t_category` |

发货请求：`{"expressCompany":"SF","expressNo":"SF123456"}`。仅 `20 已支付待发货` 可发货，成功后写入 `deliveryTime` 并转为 30。

## 5. 建议的响应对象

商品详情至少包含：`id`、`name`、`subtitle`、`mainImage`、`detail`、`priceMin`、`priceMax`、`sales`、`skus[]`、`images[]`、`reviewSummary`。订单详情至少包含：`orderNo`、金额字段、`status`、收货快照、`items[]`、物流字段和关键时间；禁止直接暴露数据库内部密码、逻辑删除字段和库存版本号。

响应字段类型约定（服务端全局生效，前端按此解析）：

- 所有金额字段（`price`、`priceMin`、`priceMax`、`payAmount`、`avgRating` 等）序列化为**字符串**，如 `"8999.00"`，保留数据库 DECIMAL 标度；入参同样接受字符串。
- SKU 的 `specs` 返回**对象**（如 `{"颜色":"黑","容量":"256G"}`），不是需要二次 `JSON.parse` 的字符串；无规格时为 `{}`。
- 评价的 `images` 返回 URL 字符串数组，无图为 `[]`。
- 分类树的叶子节点 `children` 为 `[]`（不返回 `null`）。

## 6. 实现验收清单

1. 同一用户同一 SKU 加购并发请求最终只有一条购物车记录（依赖 `uk_user_sku`）。
2. 两个并发订单购买最后 1 件库存时只有一个成功，失败方返回 `40900`，不产生半订单。
3. 支付回调重复投递不会重复改订单状态或生成销量。
4. 改价、改名、修改地址后，历史订单仍展示下单快照。
5. 普通用户不能读取或修改其他用户订单、地址和购物车；管理员接口不能被用户 Token 调用。
6. 订单状态转换、评价唯一性、管理员发货均有服务端校验，不能只依赖前端按钮控制。
