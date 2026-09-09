package com.yuki.shopping.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/** 全局 JSON 约定：BigDecimal 序列化为字符串（接口文档 1 节，避免 JS 浮点误差）；时间统一 yyyy-MM-dd HH:mm:ss */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public SimpleModule bigDecimalToStringModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(BigDecimal.class, ToStringSerializer.instance);
        return module;
    }

    /** 覆盖 LocalDateTime 的 ISO 默认格式（2026-09-09T19:50:22 → 2026-09-09 19:50:22） */
    @Bean
    public Module javaTimeFormatModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        module.addDeserializer(java.time.LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        return module;
    }
}
