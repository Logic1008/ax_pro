package com.axiang.ax_pro.common;

import java.time.Instant;

/**
 * 统一响应结构
 * code：业务状态码（如 OK/NOT_FOUND/UNAUTHORIZED）
 * message：提示信息
 * data：业务数据载荷
 * timestamp：服务端时间戳，便于前端排查与记录
 */
public class ApiResponse<T> {
    private final String code;
    private final String message;
    private final T data;
    private final long timestamp;

    /**
     * 构造响应对象（私有），建议通过工厂方法创建
     */
    private ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now().toEpochMilli();
    }

    /**
     * 成功响应（默认消息）
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("OK", "success", data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("OK", message, data);
    }

    /**
     * 错误响应（含错误码与错误描述）
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /** 业务状态码 */
    public String getCode() {
        return code;
    }

    /** 提示信息 */
    public String getMessage() {
        return message;
    }

    /** 数据载荷 */
    public T getData() {
        return data;
    }

    /** 服务端时间戳（毫秒） */
    public long getTimestamp() {
        return timestamp;
    }
}
