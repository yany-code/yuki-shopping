package com.yuki.shopping;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("com.yuki.shopping.**.mapper")
@ConfigurationPropertiesScan
public class YukiShoppingApplication {
    public static void main(String[] args) { SpringApplication.run(YukiShoppingApplication.class, args); }
}
