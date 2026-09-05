package com.yuki.shopping.modules.admin.controller;
import com.yuki.shopping.common.api.ApiResponse; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/admin") public class AdminController {
 @GetMapping("/products") public ApiResponse<?> products(){return ApiResponse.ok(List.of());}
 @GetMapping("/orders") public ApiResponse<?> orders(){return ApiResponse.ok(List.of());}
 @PostMapping("/orders/{orderNo}/ship") public ApiResponse<?> ship(@PathVariable String orderNo,@RequestBody Map<String,String> body){return ApiResponse.ok();}
 @GetMapping("/reviews") public ApiResponse<?> reviews(){return ApiResponse.ok(List.of());}
 @PutMapping("/reviews/{id}/status") public ApiResponse<?> reviewStatus(@PathVariable Long id,@RequestBody Map<String,Integer> body){return ApiResponse.ok();}
}
