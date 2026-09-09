package com.yuki.shopping.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuki.shopping.common.exception.BusinessException;
import com.yuki.shopping.common.security.PayProperties;
import com.yuki.shopping.common.security.SecurityUtils;
import com.yuki.shopping.modules.order.domain.Order;
import com.yuki.shopping.modules.order.domain.OrderItem;
import com.yuki.shopping.modules.order.domain.OrderPayDTO;
import com.yuki.shopping.modules.order.domain.OrderStatusMachine;
import com.yuki.shopping.modules.order.domain.PayResultVO;
import com.yuki.shopping.modules.order.domain.Payment;
import com.yuki.shopping.modules.order.domain.PaymentCallbackDTO;
import com.yuki.shopping.modules.order.mapper.OrderItemMapper;
import com.yuki.shopping.modules.order.mapper.OrderMapper;
import com.yuki.shopping.modules.order.mapper.PaymentMapper;
import com.yuki.shopping.modules.order.service.PaymentService;
import com.yuki.shopping.modules.product.domain.Product;
import com.yuki.shopping.modules.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final StringRedisTemplate redisTemplate;
    private final PayProperties payProperties;

    @Override
    public PayResultVO pay(String orderNo, OrderPayDTO request, String idempotencyKey) {
        if (request.getPayType() < 1 || request.getPayType() > 3) {
            throw new BusinessException(40000, "payType 取值 1~3");
        }
        Order order = requireOwnedOrder(orderNo);
        // 幂等键按 用户+订单 隔离：同键重试返回首次创建的流水（文档 2.3：支付创建幂等，重复请求返回第一次成功结果）
        String idemKey = idempotencyKey == null || idempotencyKey.isBlank()
                ? null : PAY_IDEM_KEY_PREFIX + order.getUserId() + ":" + order.getOrderNo() + ":" + idempotencyKey;
        if (idemKey != null) {
            String cachedPaymentNo = redisTemplate.opsForValue().get(idemKey);
            if (cachedPaymentNo != null) {
                Payment cached = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getPaymentNo, cachedPaymentNo));
                if (cached != null) {
                    return toPayResult(cached); // 命中缓存直接返回首次结果，不再走状态机与新建
                }
                redisTemplate.delete(idemKey); // 流水不存在（理论不发生）：清脏缓存走正常创建
            }
        }
        // 状态机校验：仅待支付可发起；未带幂等键的重复点击支付在此被挡
        OrderStatusMachine.checkTransition(order.getStatus(), Order.STATUS_PAID);

        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(order.getUserId());
        payment.setPayType(request.getPayType());
        // 金额从订单读取，绝不接受客户端传入
        payment.setAmount(order.getPayAmount());
        payment.setStatus(Payment.STATUS_PROCESSING);
        paymentMapper.insert(payment);

        if (idemKey != null) {
            // pay 无事务，insert 即已提交，先落库后写缓存不存在幻影缓存
            redisTemplate.opsForValue().set(idemKey, payment.getPaymentNo(), PAY_IDEM_TTL);
        }
        return toPayResult(payment); // payParams 模拟支付为空；真实网关在此返回拉起收银台的参数
    }

    @Override
    @Transactional
    public void handleCallback(PaymentCallbackDTO payload) {
        // 1. 验签：MD5(paymentNo|status|amount|密钥)，金额入签双重保险；
        // permitAll 接口以验签为鉴权
        String expected = sign(payload.getPaymentNo(), payload.getStatus(), payload.getAmount());
        if (!expected.equals(payload.getSign())) {
            throw new BusinessException(40300, "回调签名校验失败");
        }
        // 2. 定位支付流水
        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getPaymentNo, payload.getPaymentNo()));
        if (payment == null) {
            throw new BusinessException(40400, "支付单不存在");
        }
        // 3. 金额二次比对：必须 compareTo（BigDecimal.equals 连 scale 一起比）
        if (payment.getAmount().compareTo(payload.getAmount()) != 0) {
            throw new BusinessException(40900, "回调金额与支付单不符");
        }
        if (payload.getStatus() == Payment.STATUS_FAILED) {
            markFailed(payment);
            return;
        }
        // 4. 幂等闸门：仅当流水仍为"处理中"才推进（验收项 3 的核心）。
        // 放在状态机校验之前：重复成功回调（流水已成功）在此直接返回 code:0，
        // 不给网关非成功响应，避免其持续重推造成告警风暴
        Payment success = new Payment();
        success.setStatus(Payment.STATUS_SUCCESS);
        success.setTradeNo(payload.getTradeNo());
        success.setPaidAt(LocalDateTime.now());
        int rows = paymentMapper.update(success, new LambdaUpdateWrapper<Payment>()
                .eq(Payment::getId, payment.getId())
                .eq(Payment::getStatus, Payment.STATUS_PROCESSING));
        if (rows == 0) {
            return;
        }
        // 5. 推进订单 10 → 20（条件更新；订单已被取消等并发场景抛出会连闸门一起回滚）
        Order order = orderMapper.selectById(payment.getOrderId());
        OrderStatusMachine.checkTransition(order.getStatus(), Order.STATUS_PAID);
        Order orderUpdate = new Order();
        orderUpdate.setId(order.getId());
        orderUpdate.setStatus(Order.STATUS_PAID);
        orderUpdate.setPayTime(LocalDateTime.now());
        int orderRows = orderMapper.update(orderUpdate, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, order.getId())
                .eq(Order::getStatus, Order.STATUS_UNPAID));
        if (orderRows == 0) {
            throw new BusinessException(40900, "订单状态已变更，无法完成支付");
        }
        // 6. 累加销量：quantity 来自服务端库表，拼接安全；快照订单不受影响
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            productMapper.update(null, new LambdaUpdateWrapper<Product>()
                    .setSql("sales = sales + " + item.getQuantity())
                    .eq(Product::getId, item.getProductId()));
        }
    }

    /** 回调签名：MD5(paymentNo|status|amount|密钥)，32 位小写；amount 用 toPlainString 保证与网关拼接一致 */
    private String sign(String paymentNo, Integer status, java.math.BigDecimal amount) {
        String material = paymentNo + "|" + status + "|" + amount.toPlainString() + "|" + payProperties.getSecret();
        return DigestUtils.md5DigestAsHex(material.getBytes(StandardCharsets.UTF_8));
    }

    private PayResultVO toPayResult(Payment payment) {
        PayResultVO vo = new PayResultVO();
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setPayType(payment.getPayType());
        // 过期时间从流水创建时间起算（而非"现在"），重试返回同一结果时口径一致
        vo.setExpireTime(payment.getCreatedAt().plusMinutes(payProperties.getExpireMinutes()));
        return vo;
    }

    private void markFailed(Payment payment) {
        Payment failed = new Payment();
        failed.setStatus(Payment.STATUS_FAILED);
        paymentMapper.update(failed, new LambdaUpdateWrapper<Payment>()
                .eq(Payment::getId, payment.getId())
                .eq(Payment::getStatus, Payment.STATUS_PROCESSING));
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

    private String generatePaymentNo() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(100000, 1000000);
    }

    private static final String PAY_IDEM_KEY_PREFIX = "pay:idem:";
    private static final Duration PAY_IDEM_TTL = Duration.ofHours(24);
}
