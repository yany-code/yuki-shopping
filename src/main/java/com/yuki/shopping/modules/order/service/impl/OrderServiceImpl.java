package com.yuki.shopping.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuki.shopping.common.api.PageResult;
import com.yuki.shopping.common.exception.BusinessException;
import com.yuki.shopping.common.security.SecurityUtils;
import com.yuki.shopping.modules.cart.domain.CartItem;
import com.yuki.shopping.modules.cart.mapper.CartItemMapper;
import com.yuki.shopping.modules.order.domain.CreateOrderResult;
import com.yuki.shopping.modules.order.domain.Order;
import com.yuki.shopping.modules.order.domain.OrderCreateDTO;
import com.yuki.shopping.modules.order.domain.OrderDetailVO;
import com.yuki.shopping.modules.order.domain.OrderItem;
import com.yuki.shopping.modules.order.domain.OrderItemDTO;
import com.yuki.shopping.modules.order.domain.OrderItemVO;
import com.yuki.shopping.modules.order.domain.OrderListVO;
import com.yuki.shopping.modules.order.domain.OrderPreviewVO;
import com.yuki.shopping.modules.order.domain.OrderStatusMachine;
import com.yuki.shopping.modules.order.domain.Payment;
import com.yuki.shopping.modules.order.mapper.OrderItemMapper;
import com.yuki.shopping.modules.order.mapper.OrderMapper;
import com.yuki.shopping.modules.order.mapper.PaymentMapper;
import com.yuki.shopping.modules.order.service.OrderService;
import com.yuki.shopping.modules.product.domain.Product;
import com.yuki.shopping.modules.product.domain.Sku;
import com.yuki.shopping.modules.product.mapper.ProductMapper;
import com.yuki.shopping.modules.product.mapper.SkuMapper;
import com.yuki.shopping.modules.user.domain.UserAddress;
import com.yuki.shopping.modules.user.mapper.UserAddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    /** 运费规则（接口文档未定义，显式声明）：满 99 包邮，否则 10 元 */
    private static final BigDecimal FREIGHT_FREE_THRESHOLD = new BigDecimal("99.00");
    private static final BigDecimal FREIGHT_FEE = new BigDecimal("10.00");
    /** 金额零值固定两位小数，避免序列化成 "0"（与库内 DECIMAL 口径一致） */
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private static final String IDEM_KEY_PREFIX = "order:idem:";
    private static final Duration IDEM_TTL = Duration.ofHours(24);
    private static final long MAX_PAGE_SIZE = 100;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final PaymentMapper paymentMapper;
    private final SkuMapper skuMapper;
    private final ProductMapper productMapper;
    private final UserAddressMapper addressMapper;
    private final CartItemMapper cartItemMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /** 试算中间产物：校验通过的 SKU/商品 + 数量（金额一律以这里的服务端实时数据为准） */
    private record OrderDraft(Sku sku, Product product, int quantity) { }

    @Override
    public OrderPreviewVO preview(OrderCreateDTO request) {
        requireOwnedAddress(request.getAddressId()); // 提前暴露地址问题
        List<OrderDraft> drafts = loadDrafts(request.getItems());
        BigDecimal totalAmount = totalOf(drafts);
        BigDecimal freightAmount = freightOf(totalAmount);
        OrderPreviewVO vo = new OrderPreviewVO();
        vo.setItems(drafts.stream().map(this::toPreviewItemVO).toList());
        vo.setTotalAmount(totalAmount);
        vo.setFreightAmount(freightAmount);
        vo.setPayAmount(totalAmount.add(freightAmount)); // 优惠暂为 0
        return vo;
    }

    @Override
    @Transactional
    public CreateOrderResult create(OrderCreateDTO request, String idempotencyKey) {
        Long userId = SecurityUtils.currentUserId();
        // 幂等：同 Idempotency-Key 的重试直接返回首次结果；键按用户隔离，值只存订单号
        String idemKey = idempotencyKey == null || idempotencyKey.isBlank()
                ? null : IDEM_KEY_PREFIX + userId + ":" + idempotencyKey;
        if (idemKey != null) {
            String cachedOrderNo = redisTemplate.opsForValue().get(idemKey);
            if (cachedOrderNo != null) {
                Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, cachedOrderNo));
                if (order == null) {
                    throw new BusinessException(40400, "订单不存在");
                }
                return toResult(order);
            }
        }

        UserAddress address = requireOwnedAddress(request.getAddressId());
        List<OrderDraft> drafts = loadDrafts(request.getItems()); // 已按 skuId 排序，并发扣库存加锁顺序一致

        // 条件扣库存：stock >= quantity 才更新；任一失败抛 40900，整单回滚（验收项 2）
        for (OrderDraft d : drafts) {
            int rows = skuMapper.deductStock(d.sku().getId(), d.quantity());
            if (rows == 0) {
                throw new BusinessException(40900, "库存不足: " + d.product().getName());
            }
        }

        BigDecimal totalAmount = totalOf(drafts);
        BigDecimal freightAmount = freightOf(totalAmount);
        BigDecimal discountAmount = ZERO;

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setFreightAmount(freightAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(totalAmount.add(freightAmount).subtract(discountAmount));
        order.setStatus(Order.STATUS_UNPAID);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(joinAddress(address));
        order.setRemark(request.getRemark());
        orderMapper.insert(order);

        // 明细快照：创建后不可变，商品改名改价不影响历史订单（验收项 4）
        for (OrderDraft d : drafts) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setUserId(userId);
            item.setProductId(d.product().getId());
            item.setSkuId(d.sku().getId());
            item.setProductName(d.product().getName());
            item.setSkuSpecs(d.sku().getSpecs());
            item.setImage(d.sku().getImage() != null ? d.sku().getImage() : d.product().getMainImage());
            item.setPrice(d.sku().getPrice());
            item.setQuantity(d.quantity());
            item.setSubtotal(d.sku().getPrice().multiply(BigDecimal.valueOf(d.quantity())));
            orderItemMapper.insert(item);
        }

        // 结算完成，按 skuId 移除购物车条目
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getSkuId, drafts.stream().map(d -> d.sku().getId()).toList()));

        CreateOrderResult result = toResult(order);
        if (idemKey != null) {
            // 严格实现应在事务提交后再写缓存；此处毫秒级窗口可接受
            redisTemplate.opsForValue().set(idemKey, order.getOrderNo(), IDEM_TTL);
        }
        return result;
    }

    @Override
    public PageResult<OrderListVO> page(long page, long pageSize, Integer status) {
        Page<Order> result = orderMapper.selectPage(
                new Page<>(Math.max(page, 1), Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE)),
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, SecurityUtils.currentUserId())
                        .eq(status != null, Order::getStatus, status)
                        .orderByDesc(Order::getId));
        // 批量补全明细，一次 IN 查询替代 N 次单查
        List<Order> orders = result.getRecords();
        Map<Long, List<OrderItem>> itemsByOrderId = orders.isEmpty() ? Map.of()
                : orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                        .in(OrderItem::getOrderId, orders.stream().map(Order::getId).toList()))
                .stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
        List<OrderListVO> vos = orders.stream()
                .map(o -> toListVO(o, itemsByOrderId.getOrDefault(o.getId(), List.of())))
                .toList();
        return new PageResult<>(vos, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public OrderDetailVO detail(String orderNo) {
        Order order = requireOwnedOrder(orderNo);
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId())
                .orderByAsc(OrderItem::getId));
        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderNo, order.getOrderNo())
                .orderByDesc(Payment::getId)
                .last("LIMIT 1")); // 最新一条流水；last 仅拼固定片段

        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setRemark(order.getRemark());
        vo.setExpressCompany(order.getExpressCompany());
        vo.setExpressNo(order.getExpressNo());
        vo.setPayTime(order.getPayTime());
        vo.setDeliveryTime(order.getDeliveryTime());
        vo.setFinishTime(order.getFinishTime());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setItems(items.stream().map(this::toItemVO).toList());
        if (payment != null) {
            vo.setPaymentNo(payment.getPaymentNo());
            vo.setPayType(payment.getPayType());
        }
        return vo;
    }

    @Override
    @Transactional
    public void cancel(String orderNo) {
        Order order = requireOwnedOrder(orderNo);
        OrderStatusMachine.checkTransition(order.getStatus(), Order.STATUS_CANCELED); // 仅 10 → 50
        releaseStock(order.getId());
        Order update = new Order();
        update.setId(order.getId());
        update.setStatus(Order.STATUS_CANCELED);
        update.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(update);
    }

    @Override
    public void confirm(String orderNo) {
        Order order = requireOwnedOrder(orderNo);
        OrderStatusMachine.checkTransition(order.getStatus(), Order.STATUS_FINISHED); // 仅 30 → 40
        Order update = new Order();
        update.setId(order.getId());
        update.setStatus(Order.STATUS_FINISHED);
        update.setFinishTime(LocalDateTime.now());
        orderMapper.updateById(update); // 单条写无需事务
    }

    @Override
    @Transactional
    public int cancelTimeoutOrders(int timeoutMinutes) {
        List<Order> timeout = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, Order.STATUS_UNPAID)
                .lt(Order::getCreatedAt, LocalDateTime.now().minusMinutes(timeoutMinutes))
                .last("LIMIT 100"));
        int cancelled = 0;
        for (Order order : timeout) {
            // 条件更新抢闸门：仍为待支付才取消，防与用户支付/取消并发
            Order patch = new Order();
            patch.setStatus(Order.STATUS_CANCELED);
            patch.setCancelTime(LocalDateTime.now());
            int rows = orderMapper.update(patch, new LambdaUpdateWrapper<Order>()
                    .eq(Order::getId, order.getId())
                    .eq(Order::getStatus, Order.STATUS_UNPAID));
            if (rows > 0) {
                releaseStock(order.getId());
                cancelled++;
            }
        }
        return cancelled;
    }

    // ---------- 私有方法 ----------

    private CreateOrderResult toResult(Order order) {
        CreateOrderResult result = new CreateOrderResult();
        result.setOrderNo(order.getOrderNo());
        result.setStatus(order.getStatus());
        result.setPayAmount(order.getPayAmount());
        return result;
    }

    private void releaseStock(Long orderId) {
        orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId))
                .forEach(item -> skuMapper.restoreStock(item.getSkuId(), item.getQuantity()));
    }

    /** 加载并校验订单商品：去重 → 批量查 SKU/商品 → 校验存在与可售 → 按 skuId 排序 */
    private List<OrderDraft> loadDrafts(List<OrderItemDTO> items) {
        Set<Long> skuIds = new HashSet<>();
        for (OrderItemDTO item : items) {
            if (!skuIds.add(item.getSkuId())) {
                throw new BusinessException(40000, "items 中存在重复 SKU");
            }
        }
        Map<Long, Sku> skuMap = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                        .in(Sku::getId, skuIds))
                .stream().collect(Collectors.toMap(Sku::getId, Function.identity()));
        // 空集合不能进 in()（生成非法 SQL 变 50000）：SKU 全部不存在时直接空 Map，交给下方校验抛 40400/40900
        List<Long> productIds = skuMap.values().stream().map(Sku::getProductId).distinct().toList();
        Map<Long, Product> productMap = productIds.isEmpty() ? Map.of()
                : productMapper.selectList(new LambdaQueryWrapper<Product>()
                        .in(Product::getId, productIds))
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        List<OrderDraft> drafts = new ArrayList<>();
        for (OrderItemDTO item : items) {
            Sku sku = skuMap.get(item.getSkuId());
            Product product = sku == null ? null : productMap.get(sku.getProductId());
            if (sku == null) {
                throw new BusinessException(40400, "商品规格不存在");
            }
            if (product == null || product.getStatus() != 1 || sku.getStatus() != 1) {
                throw new BusinessException(40900, "商品已下架: "
                        + (product == null ? "" : product.getName()));
            }
            drafts.add(new OrderDraft(sku, product, item.getQuantity()));
        }
        drafts.sort(Comparator.comparing(d -> d.sku().getId()));
        return drafts;
    }

    private BigDecimal totalOf(List<OrderDraft> drafts) {
        BigDecimal total = ZERO;
        for (OrderDraft d : drafts) {
            total = total.add(d.sku().getPrice().multiply(BigDecimal.valueOf(d.quantity())));
        }
        return total;
    }

    private BigDecimal freightOf(BigDecimal totalAmount) {
        return totalAmount.compareTo(FREIGHT_FREE_THRESHOLD) >= 0 ? ZERO : FREIGHT_FEE;
    }

    private UserAddress requireOwnedAddress(Long addressId) {
        UserAddress address = addressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException(40400, "收货地址不存在");
        }
        if (!address.getUserId().equals(SecurityUtils.currentUserId())) {
            throw new BusinessException(40300, "无权限使用该地址");
        }
        return address;
    }

    private Order requireOwnedOrder(String orderNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(40400, "订单不存在");
        }
        if (!order.getUserId().equals(SecurityUtils.currentUserId())) {
            throw new BusinessException(40300, "无权限操作该订单");
        }
        return order;
    }

    private String joinAddress(UserAddress address) {
        return String.join(" ", address.getProvince(), address.getCity(),
                address.getDistrict(), address.getDetail());
    }

    /** 订单号：yyyyMMddHHmmss + 6 位随机；uk_order_no 唯一索引兜底 */
    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(100000, 1000000);
    }

    private OrderListVO toListVO(Order order, List<OrderItem> items) {
        OrderListVO vo = new OrderListVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setItems(items.stream().map(this::toItemVO).toList());
        return vo;
    }

    private OrderItemVO toItemVO(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setSkuId(item.getSkuId());
        vo.setProductName(item.getProductName());
        vo.setSkuSpecs(parseSpecs(item.getSkuSpecs()));
        vo.setImage(item.getImage());
        vo.setPrice(item.getPrice());
        vo.setQuantity(item.getQuantity());
        vo.setSubtotal(item.getSubtotal());
        return vo;
    }

    private OrderItemVO toPreviewItemVO(OrderDraft d) {
        OrderItemVO vo = new OrderItemVO();
        vo.setProductId(d.product().getId());
        vo.setSkuId(d.sku().getId());
        vo.setProductName(d.product().getName());
        vo.setSkuSpecs(parseSpecs(d.sku().getSpecs()));
        vo.setImage(d.sku().getImage() != null ? d.sku().getImage() : d.product().getMainImage());
        vo.setPrice(d.sku().getPrice());
        vo.setQuantity(d.quantity());
        vo.setSubtotal(d.sku().getPrice().multiply(BigDecimal.valueOf(d.quantity())));
        return vo;
    }

    /** 库里 specs 是 JSON 字符串，VO 输出结构化对象，与购物车/商品详情接口口径一致 */
    private Map<String, String> parseSpecs(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() { });
        } catch (Exception e) {
            return Map.of(); // 脏数据不打挂接口
        }
    }
}
