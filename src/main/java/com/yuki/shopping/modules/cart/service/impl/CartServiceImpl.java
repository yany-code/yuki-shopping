package com.yuki.shopping.modules.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuki.shopping.common.exception.BusinessException;
import com.yuki.shopping.common.security.SecurityUtils;
import com.yuki.shopping.modules.cart.domain.CartCheckAllDTO;
import com.yuki.shopping.modules.cart.domain.CartItem;
import com.yuki.shopping.modules.cart.domain.CartItemAddDTO;
import com.yuki.shopping.modules.cart.domain.CartItemUpdateDTO;
import com.yuki.shopping.modules.cart.domain.CartItemVO;
import com.yuki.shopping.modules.cart.mapper.CartItemMapper;
import com.yuki.shopping.modules.cart.service.CartService;
import com.yuki.shopping.modules.product.domain.Product;
import com.yuki.shopping.modules.product.domain.Sku;
import com.yuki.shopping.modules.product.mapper.ProductMapper;
import com.yuki.shopping.modules.product.mapper.SkuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final SkuMapper skuMapper;
    private final ProductMapper productMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<CartItemVO> list() {
        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, SecurityUtils.currentUserId())
                .orderByDesc(CartItem::getId));
        if (items.isEmpty()) {
            return List.of();
        }
        // 批量补全：两次 IN 查询替代 N 次单查（阶段 2 评价列表同款手法）
        List<Long> skuIds = items.stream().map(CartItem::getSkuId).toList();
        Map<Long, Sku> skuMap = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                        .in(Sku::getId, skuIds))
                .stream().collect(Collectors.toMap(Sku::getId, Function.identity()));
        List<Long> productIds = skuMap.values().stream()
                .map(Sku::getProductId).distinct().toList();
        // 逻辑删除自动生效：已删商品查不到 → onSale=false；空集合不能进 in()（生成非法 SQL 变 50000）
        Map<Long, Product> productMap = productIds.isEmpty() ? Map.of()
                : productMapper.selectList(new LambdaQueryWrapper<Product>()
                        .in(Product::getId, productIds))
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        return items.stream().map(item -> {
            Sku sku = skuMap.get(item.getSkuId());
            Product product = sku == null ? null : productMap.get(sku.getProductId());
            return toVO(item, sku, product);
        }).toList();
    }

    @Override
    public CartItemVO add(CartItemAddDTO request) {
        Long userId = SecurityUtils.currentUserId();
        Sku sku = skuMapper.selectById(request.getSkuId());
        if (sku == null) {
            throw new BusinessException(40400, "商品规格不存在");
        }
        Product product = productMapper.selectById(sku.getProductId());
        // 只校验可售状态，不校验库存：最终库存以创建订单时条件扣减为准（接口文档 3.3）
        if (product == null || product.getStatus() != 1 || sku.getStatus() != 1) {
            throw new BusinessException(40900, "商品已下架或规格已禁用");
        }
        // checked 已由 DTO @Min/@Max 校验，缺省默认勾选；重复加购在 SQL 的 UPDATE 分支恢复勾选
        int checked = request.getChecked() == null ? 1 : request.getChecked();
        cartItemMapper.upsertAdd(userId, sku.getId(), request.getQuantity(), checked);
        CartItem item = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSkuId, sku.getId()));
        return toVO(item, sku, product);
    }

    @Override
    public CartItemVO update(Long id, CartItemUpdateDTO request) {
        CartItem item = requireOwnedCartItem(id);
        if (request.getQuantity() != null) {
            item.setQuantity(request.getQuantity());
        }
        if (request.getChecked() != null) {
            item.setChecked(request.getChecked());
        }
        if (request.getQuantity() != null || request.getChecked() != null) {
            cartItemMapper.updateById(item);
        }
        Sku sku = skuMapper.selectById(item.getSkuId());
        Product product = sku == null ? null : productMapper.selectById(sku.getProductId());
        return toVO(item, sku, product);
    }

    @Override
    public void delete(Long id) {
        requireOwnedCartItem(id);
        // t_cart_item 无 deleted 列 → 物理删除：临时数据无恢复/审计需求
        cartItemMapper.deleteById(id);
    }

    @Override
    public void checkAll(CartCheckAllDTO request) {
        CartItem patch = new CartItem();
        patch.setChecked(request.getChecked()); // DTO @Min/@Max 已保证 0/1
        // 批量隔离靠 WHERE user_id，单条 UPDATE 天然原子，无需事务
        cartItemMapper.update(patch, new LambdaUpdateWrapper<CartItem>()
                .eq(CartItem::getUserId, SecurityUtils.currentUserId()));
    }

    /**
     *归属校验统一入口：不存在 40400，不属于当前用户 40300（与地址模块同一套约定）
     */
    private CartItem requireOwnedCartItem(Long id) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(40400, "购物车条目不存在");
        }
        if (!item.getUserId().equals(SecurityUtils.currentUserId())) {
            throw new BusinessException(40300, "无权限操作该购物车条目");
        }
        return item;
    }

    /**
     * 库里 specs 是 JSON 字符串，VO 输出结构化对象，与详情接口 SkuVO.specs 口径一致
     */
    private Map<String, String> parseSpecs(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {//String 转 Map,方便拿取
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() { });
        } catch (Exception e) {
            return Map.of(); // 脏数据不打挂接口
        }
    }

    /**
     * 组装实时状态：条目数据来自购物车表，商品/规格/价格来自商品域实时读取
     */
    private CartItemVO toVO(CartItem item, Sku sku, Product product) {
        CartItemVO vo = new CartItemVO();

        vo.setId(item.getId());
        vo.setSkuId(item.getSkuId());
        vo.setQuantity(item.getQuantity());
        vo.setChecked(item.getChecked());

        //规格/商品查询
        boolean onSale = sku != null && sku.getStatus() == 1
                && product != null && product.getStatus() == 1;
        vo.setOnSale(onSale);

         //存量查询
        vo.setStockEnough(onSale && sku.getStock() >= item.getQuantity());


        if (sku != null) {
            vo.setProductId(sku.getProductId());
            vo.setSkuSpecs(parseSpecs(sku.getSpecs()));
            vo.setPrice(sku.getPrice());
            String image = sku.getImage();
            if (image == null && product != null) {
                image = product.getMainImage(); // SKU 图缺失时退回商品主图
            }
            vo.setImage(image);
        }

        if (product != null) {
            vo.setProductName(product.getName());
        }

        return vo;
    }
}
