package com.axiang.ax_pro.common;

import jakarta.validation.ConstraintViolationException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * 全局异常处理
 * 将常见异常统一映射为标准响应结构，避免泄漏内部细节并保持接口一致性
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /** 参数校验失败（@Valid/@Validated） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .orElse("validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("BAD_REQUEST", msg));
    }

    /** 约束违反（如 @Positive 等） */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .orElse("validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("BAD_REQUEST", msg));
    }

    /** 业务异常（带错误码与 HTTP 状态） */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /** 资源未找到 */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", ex.getMessage()));
    }

    /** 未捕获的系统异常，统一返回 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "internal server error"));
    }

    /** 限流异常，返回 429 */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiResponse<Object>> handleTooManyRequests(RequestNotPermitted ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error("TOO_MANY_REQUESTS", "rate limit exceeded"));
    }
}
