package com.yuki.shopping.modules.cart.controller;
import com.yuki.shopping.common.api.ApiResponse;
import jakarta.validation.Valid; import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/cart")
public class CartController {
    public record ItemRequest(@NotNull Long skuId, @Min(1) Integer quantity, Boolean checked) {}
    @GetMapping public ApiResponse<?> list() { return ApiResponse.ok(List.of()); }
    @PostMapping("/items") public ApiResponse<?> add(@Valid @RequestBody ItemRequest r) { return ApiResponse.ok(r); }
    @PutMapping("/items/{id}") public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody ItemRequest r) { return ApiResponse.ok(r); }
    @DeleteMapping("/items/{id}") public ApiResponse<?> delete(@PathVariable Long id) { return ApiResponse.ok(); }
    @PutMapping("/items/check-all") public ApiResponse<?> checkAll(@RequestBody Map<String,Boolean> body) { return ApiResponse.ok(); }
}
