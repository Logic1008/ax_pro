package com.axiang.ax_pro.common;

import org.springframework.http.HttpStatus;

/**
 * 业务异常
 * 封装业务错误码与对应的 HTTP 状态，用于统一异常处理与标准化输出
 */
public class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    /**
     * 默认业务异常，错误码为 BUSINESS_ERROR，HTTP 状态 400
     */
    public BusinessException(String message) {
        this("BUSINESS_ERROR", message, HttpStatus.BAD_REQUEST);
    }

    /**
     * 自定义业务异常，支持指定错误码与 HTTP 状态
     */
    public BusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    /** 错误码 */
    public String getCode() {
        return code;
    }

    /** HTTP 状态 */
    public HttpStatus getStatus() {
        return status;
    }
}
