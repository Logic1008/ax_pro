package com.axiang.ax_pro.config;

import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/**
 * Sa-Token 安全配置
 * - 包含路径：/api/**
 * - 排除路径：认证与只读接口（/api/auth/**、/api/tests、/api/tests/*、/api/tests/*/result/*）
 * - 认证策略：除白名单外统一要求登录（StpUtil.checkLogin）
 * - 错误输出：401 + 标准 JSON
 */
public class SaTokenConfig {
    @Bean
    /** 配置 Sa-Token 过滤器与路由拦截规则 */
    public SaServletFilter saServletFilter() {
        return new SaServletFilter()
                .addInclude("/api/**")
                .addExclude("/api/auth/**", "/api/tests", "/api/tests/*", "/api/tests/*/result/*")
                .setAuth(obj -> StpUtil.checkLogin())
                .setError(e -> {
                    cn.dev33.satoken.context.SaHolder.getResponse().setStatus(401);
                    return "{\"code\":\"UNAUTHORIZED\",\"message\":\"login required\"}";
                });
    }
}

