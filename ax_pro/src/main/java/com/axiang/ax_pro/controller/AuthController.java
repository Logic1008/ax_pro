package com.axiang.ax_pro.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.axiang.ax_pro.common.ApiResponse;
import com.axiang.ax_pro.dto.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
/**
 * 认证控制器
 * 提供登录接口，成功后返回 Sa-Token 的 token（仅从 Header 读取）
 */
public class AuthController {
    /**
     * 登录接口
     * 入参：用户名/密码（示例账号 test/123456）
     * 成功：返回 { token }，前端将其放入请求头 token
     * 失败：401 UNAUTHORIZED
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Validated @RequestBody LoginRequest req) {
        if ("test".equals(req.getUsername()) && "123456".equals(req.getPassword())) {
            StpUtil.login(1L);
            Map<String, Object> map = new HashMap<>();
            map.put("token", StpUtil.getTokenValue());
            return ResponseEntity.ok(ApiResponse.success(map));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("UNAUTHORIZED", "invalid credentials"));
    }
}

