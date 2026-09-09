package com.yuki.shopping.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuki.shopping.common.api.PageResult;
import com.yuki.shopping.common.exception.BusinessException;
import com.yuki.shopping.modules.product.domain.*;
import com.yuki.shopping.modules.product.mapper.*;
import com.yuki.shopping.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final long MAX_PAGE_SIZE = 100;

    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final ProductImageMapper productImageMapper;
    private final ReviewMapper reviewMapper;
    private final ObjectMapper objectMapper;

    /**
     * 建立分类树
     * @return
     */
    @Override
    public List<CategoryTreeVO> categoryTree() {
        //分类表量级很小:一次查全，内存建树，禁止逐级查库
        List<Category> all = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus,1)
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));

        Map<Long,CategoryTreeVO> voMap = new LinkedHashMap<>();

        for (Category category : all){
            CategoryTreeVO vo = new CategoryTreeVO();
            vo.setId(category.getId());
            vo.setName(category.getName());
            vo.setIcon(category.getIcon());
            voMap.put(category.getId(),vo);
        }

        List<CategoryTreeVO> roots = new ArrayList<>();

        for (Category category : all){
            CategoryTreeVO vo = voMap.get(category.getId());

            if (category.getParentId() == null || category.getParentId() == 0L) {
                roots.add(vo);
            } else {
                CategoryTreeVO parent = voMap.get(category.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo); // 父分类被禁用时子树整体不可见
                }
            }

        }

        return roots;
    }

    /**
     * 分页查询品牌
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult<BrandVO> brandPage(long page, long pageSize) {
        validatePaging(page, pageSize);
        Page<Brand> result = brandMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Brand>().orderByAsc(Brand::getId));
        List<BrandVO> vos = result.getRecords().stream().map(this::toBrandVO).toList();
        return new PageResult<>(vos, result.getCurrent(), result.getSize(), result.getTotal());
    }

    /**
     * 根据条件分页查询
     * 拼接sql语句
     * @param query
     * @return
     */
    @Override
    public PageResult<ProductListVO> page(ProductQuery query) {

        //检验分页查询页数和页大小是否合规
        validatePaging(query.getPage(), query.getPageSize());
        //检验价格范围是否合规
        validatePriceRange(query);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                // deleted = 0 由全局逻辑删除自动拼接
                .eq(Product::getStatus, 1);

        // keyword 命中名称或副标题：两个 LIKE 是 OR，整体再与其他条件 AND
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Product::getName, query.getKeyword())
                    .or().like(Product::getSubtitle, query.getKeyword()));
        }

        // 分类筛选：传任意层级都展开为其自身 + 全部后代分类（商品只挂在三级分类上）
        if (query.getCategoryId() != null) {
            wrapper.in(Product::getCategoryId, expandCategoryIds(query.getCategoryId()));
        }
        wrapper.eq(query.getBrandId() != null, Product::getBrandId, query.getBrandId())
                // 区间重叠语义：商品价区 [priceMin, priceMax] 与查询 [minPrice, maxPrice] 有交集即命中
                .ge(query.getMinPrice() != null, Product::getPriceMax, query.getMinPrice())
                .le(query.getMaxPrice() != null, Product::getPriceMin, query.getMaxPrice());

        // sort 白名单翻译，杜绝 ORDER BY 注入；各排序补 id 作稳定次序
        //排序功能
        String sort = StringUtils.hasText(query.getSort()) ? query.getSort() : "default";
        switch (sort) {
            case "price_asc" -> wrapper.orderByAsc(Product::getPriceMin).orderByAsc(Product::getId);
            case "price_desc" -> wrapper.orderByDesc(Product::getPriceMin).orderByDesc(Product::getId);
            case "sales" -> wrapper.orderByDesc(Product::getSales).orderByDesc(Product::getId);
            case "newest" -> wrapper.orderByDesc(Product::getId);
            case "default" -> wrapper.orderByDesc(Product::getSales).orderByDesc(Product::getId);
            default -> throw new BusinessException(40000, "sort 参数不合法");
        }

        Page<Product> result = productMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        List<ProductListVO> vos = result.getRecords().stream().map(this::toListVO).toList();

        return new PageResult<>(vos, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public ProductDetailVO detail(Long productId) {
        Product product = requireOnSaleProduct(productId);

        List<SkuVO> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getProductId, productId)
                        .eq(Sku::getStatus, 1) // 只展示启用 SKU
                        .orderByAsc(Sku::getPrice)
                        .orderByAsc(Sku::getId))
                .stream().map(this::toSkuVO).toList();

        List<String> images = productImageMapper.selectList(new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .orderByAsc(ProductImage::getSort))
                .stream().map(ProductImage::getUrl).toList();

        ProductDetailVO vo = new ProductDetailVO();

        vo.setId(product.getId());
        vo.setCategoryId(product.getCategoryId());
        vo.setBrandId(product.getBrandId());
        vo.setName(product.getName());
        vo.setSubtitle(product.getSubtitle());
        vo.setMainImage(product.getMainImage());
        vo.setDetail(product.getDetail());
        vo.setPriceMin(product.getPriceMin());
        vo.setPriceMax(product.getPriceMax());
        vo.setSales(product.getSales());
        vo.setSkus(skus);
        vo.setImages(images);
        vo.setReviewSummary(reviewMapper.selectReviewSummary(productId));

        return vo;
    }

    @Override
    public PageResult<ReviewVO> reviewPage(Long productId, Integer rating, long page, long pageSize) {
        requireOnSaleProduct(productId); // 与详情同话术，防止借评价接口探测下架商品
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new BusinessException(40000, "rating 取值 1~5");
        }
        validatePaging(page, pageSize);
        Page<Review> result = Page.of(page, pageSize);
        IPage<Review> reviewPage = reviewMapper.selectVisiblePage(result, productId, rating);
        List<ReviewVO> vos = reviewPage.getRecords().stream().map(this::toReviewVO).toList();
        return new PageResult<>(vos, reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
    }

    /**
     * 根据id查询商品是否存在
     * @param productId
     * @return
     */
    private Product requireOnSaleProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException(40400, "商品不存在或已下架");
        }
        return product;
    }

    /**
     * 分页参数显式校验：不合规直接 40000，避免静默改写让前端无从察觉
     */
    private void validatePaging(long page, long pageSize) {
        if (page < 1) {
            throw new BusinessException(40000, "page 必须大于 0");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new BusinessException(40000, "pageSize 取值 1~" + MAX_PAGE_SIZE);
        }
    }

    /**
     * 价格查询参数显示校验
     * @param query
     */
    private void validatePriceRange(ProductQuery query) {
        if (query.getMinPrice() != null && query.getMinPrice().signum() < 0
                || query.getMaxPrice() != null && query.getMaxPrice().signum() < 0) {
            throw new BusinessException(40000, "价格不能为负数");
        }
        if (query.getMinPrice() != null && query.getMaxPrice() != null
                && query.getMinPrice().compareTo(query.getMaxPrice()) > 0) {
            throw new BusinessException(40000, "minPrice 不能大于 maxPrice");
        }
    }

    /**
     * 传任意层级分类，展开为其自身 + 全部后代分类 id（分类表极小，一次查全内存遍历）
     */
    private Set<Long> expandCategoryIds(Long categoryId) {
        List<Category> all = categoryMapper.selectList(null);
        Map<Long, List<Long>> childrenIndex = new HashMap<>();
        boolean exists = false;
        for (Category c : all) {
            if (c.getId().equals(categoryId)) {
                exists = true;
            }
            childrenIndex.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c.getId());
        }
        if (!exists) {
            throw new BusinessException(40400, "分类不存在");
        }
        Set<Long> ids = new LinkedHashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(categoryId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (ids.add(current)) { // 已访问过则跳过，防脏数据成环死循环
                queue.addAll(childrenIndex.getOrDefault(current, List.of()));
            }
        }
        return ids;
    }

    /**
     * 解析images的String json为List<String> json
     * @param json
     * @return
     */
    private List<String> parseImages(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            // 把json文本解析成java对象,变成List<String>
            return objectMapper.readValue(json, new TypeReference<List<String>>() { });
        } catch (Exception e) {
            return List.of(); // 脏数据不能打挂列表接口
        }
    }

    /**
     * 库里 specs 是 JSON 字符串，VO 输出结构化对象，前端免二次解析
     */
    private Map<String, String> parseSpecs(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() { });
        } catch (Exception e) {
            return Map.of(); // 与 parseImages 同一原则：脏数据不打挂接口
        }
    }

    /**
     * Brand 转 VO
     * @param brand
     * @return
     */
    private BrandVO toBrandVO(Brand brand) {
        BrandVO vo = new BrandVO();

        vo.setId(brand.getId());
        vo.setName(brand.getName());
        vo.setLogo(brand.getLogo());
        vo.setDescription(brand.getDescription());

        return vo;
    }

    /**
     * Product 转 ProductListVO
     * @param product
     * @return
     */
    private ProductListVO toListVO(Product product) {
        ProductListVO vo = new ProductListVO();

        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setSubtitle(product.getSubtitle());
        vo.setMainImage(product.getMainImage());
        vo.setPriceMin(product.getPriceMin());
        vo.setPriceMax(product.getPriceMax());
        vo.setSales(product.getSales());

        return vo;
    }

    /**
     * Sku 转 SkuVO
     * @param sku
     * @return
     */
    private SkuVO toSkuVO(Sku sku) {
        SkuVO vo = new SkuVO();

        vo.setId(sku.getId());
        vo.setSkuCode(sku.getSkuCode());
        vo.setSpecs(parseSpecs(sku.getSpecs()));
        vo.setPrice(sku.getPrice());
        vo.setStock(sku.getStock());
        vo.setImage(sku.getImage());

        return vo; // stockVersion / status 属于内部字段，绝不出境
    }

    /**
     * Review 转 ReviewVO
     * @param review
     * @return
     */
    private ReviewVO toReviewVO(Review review) {
        ReviewVO vo = new ReviewVO();

        vo.setId(review.getId());
        vo.setUserId(review.getUserId());
        vo.setNickname(review.getNickname());
        vo.setProductId(review.getProductId());
        vo.setSkuId(review.getSkuId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setImages(parseImages(review.getImages()));
        vo.setIsAnonymous(review.getIsAnonymous());
        vo.setStatus(review.getStatus());
        vo.setCreatedAt(review.getCreatedAt());

        return vo;
    }
}
