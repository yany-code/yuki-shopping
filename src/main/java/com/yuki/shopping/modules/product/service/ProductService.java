package com.yuki.shopping.modules.product.service;

import com.yuki.shopping.common.api.PageResult;
import com.yuki.shopping.modules.product.domain.*;

import java.util.List;

public interface ProductService {

    List<CategoryTreeVO> categoryTree();

    PageResult<BrandVO> brandPage(long page,long pageSize);

    PageResult<ProductListVO> page(ProductQuery query);

    ProductDetailVO detail(Long productId);

    PageResult<ReviewVO> reviewPage(Long productId,Integer rating,long page,long pageSize);

}
