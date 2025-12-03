package com.axiang.ax_pro.config;

import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/**
 * Sa-Token 安全配置
 * 配置接口拦截范围与提交接口的登录校验，并统一错误返回。
 */
public class SaTokenConfig {
    @Bean
    /**
     * 配置 Sa-Token 过滤器
     */
    public SaServletFilter saServletFilter() {
        return new SaServletFilter()
                .addInclude("/api/**")
                .addExclude("/api/auth/**", "/api/tests", "/api/tests/*")
                .setAuth(obj -> SaRouter.match("/api/tests/**/submit", r -> StpUtil.checkLogin()))
                .setError(e -> e.getMessage());
    }
}

