package com.yuki.shopping.modules.order.controller;
import com.yuki.shopping.common.api.*; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/orders")
public class OrderController {
    public record OrderItem(@NotNull Long skuId, @Min(1) Integer quantity) {}
    public record CreateRequest(@NotNull Long addressId, @NotEmpty List<@Valid OrderItem> items, String remark) {}
    public record PayRequest(@NotNull Integer payType) {}
    @PostMapping("/preview") public ApiResponse<?> preview(@Valid @RequestBody CreateRequest r) { return ApiResponse.ok(Map.of("items", r.items())); }
    @PostMapping public ApiResponse<?> create(@Valid @RequestBody CreateRequest r) { return ApiResponse.ok(Map.of("message", "TODO: transactional order service")); }
    @GetMapping public ApiResponse<?> page(@RequestParam(defaultValue="1") long page, @RequestParam(defaultValue="20") long pageSize, @RequestParam(required=false) Integer status) { return ApiResponse.ok(new PageResult<>(List.of(), page, pageSize, 0)); }
    @GetMapping("/{orderNo}") public ApiResponse<?> detail(@PathVariable String orderNo) { return ApiResponse.ok(); }
    @PostMapping("/{orderNo}/cancel") public ApiResponse<?> cancel(@PathVariable String orderNo) { return ApiResponse.ok(); }
    @PostMapping("/{orderNo}/confirm") public ApiResponse<?> confirm(@PathVariable String orderNo) { return ApiResponse.ok(); }
    @PostMapping("/{orderNo}/pay") public ApiResponse<?> pay(@PathVariable String orderNo, @Valid @RequestBody PayRequest r) { return ApiResponse.ok(); }
    @PostMapping("/{orderNo}/items/{itemId}/review") public ApiResponse<?> review(@PathVariable String orderNo, @PathVariable Long itemId, @RequestBody Map<String,Object> body) { return ApiResponse.ok(); }
}
