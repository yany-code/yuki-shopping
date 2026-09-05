package com.yuki.shopping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(a -> a.requestMatchers("/auth/**", "/admin/auth/**", "/categories/**", "/brands/**", "/products/**", "/payments/callback").permitAll().anyRequest().authenticated())
            .httpBasic(b -> {});
        return http.build();
    }
}
