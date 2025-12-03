package com.axiang.ax_pro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求载体
 * 携带用户名与密码，供认证接口校验。
 */
@Data
public class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}

