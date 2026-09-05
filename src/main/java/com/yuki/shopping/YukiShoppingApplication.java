package com.yuki.shopping;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yuki.shopping.**.mapper")
public class YukiShoppingApplication {
    public static void main(String[] args) { SpringApplication.run(YukiShoppingApplication.class, args); }
}
