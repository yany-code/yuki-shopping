package com.yuki.shopping.common.api;

import java.util.List;
public record PageResult<T>(List<T> list, long page, long pageSize, long total) {}
