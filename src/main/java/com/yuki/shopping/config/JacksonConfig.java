package com.yuki.shopping.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/** 全局约定：BigDecimal 序列化为字符串，避免 JS 浮点误差（接口文档 1 节）；反序列化天然兼容字符串入参 */
@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule bigDecimalToStringModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(BigDecimal.class, ToStringSerializer.instance);
        return module;
    }
}
