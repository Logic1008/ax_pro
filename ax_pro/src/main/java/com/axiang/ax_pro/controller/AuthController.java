package com.axiang.ax_pro.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.axiang.ax_pro.dto.LoginRequest;
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
 * 提供登录接口，成功后返回 Sa-Token 的 token 值。
 */
public class AuthController {
    /**
     * 登录接口
     * 路径：POST /api/auth/login
     * 入参：用户名、密码；返回：token
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Validated @RequestBody LoginRequest req) {
        if ("test".equals(req.getUsername()) && "123456".equals(req.getPassword())) {
            StpUtil.login(1L);
            Map<String, Object> map = new HashMap<>();
            map.put("token", StpUtil.getTokenValue());
            return ResponseEntity.ok(map);
        }
        return ResponseEntity.status(401).build();
    }
}

