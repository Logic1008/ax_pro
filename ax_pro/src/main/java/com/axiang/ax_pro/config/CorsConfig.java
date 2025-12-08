package com.axiang.ax_pro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 CORS 配置
 * 允许来自受控来源的跨域访问，限定方法与请求头，禁止携带凭证以适配 Header-only 令牌方案
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    /** 配置 /api/** 的跨域策略 */
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("token", "Authorization", "Content-Type")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
