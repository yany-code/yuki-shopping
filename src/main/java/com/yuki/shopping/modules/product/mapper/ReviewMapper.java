package com.yuki.shopping.modules.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuki.shopping.modules.product.domain.Review;
import com.yuki.shopping.modules.product.domain.ReviewSummaryVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ReviewMapper extends BaseMapper<Review> {

    /** 可见评价分页：仅 status=1，联查昵称并按匿名脱敏；首参 Page 由分页插件接管，XML 不写 LIMIT */
    IPage<Review> selectVisiblePage(Page<Review> page,
                                    @Param("productId") Long productId,
                                    @Param("rating") Integer rating);

    @Select("SELECT COUNT(*) AS reviewCount, IFNULL(ROUND(AVG(rating), 1), 0) AS avgRating "
            + "FROM t_review WHERE product_id = #{productId} AND status = 1")
    ReviewSummaryVO selectReviewSummary(@Param("productId") Long productId);

}
