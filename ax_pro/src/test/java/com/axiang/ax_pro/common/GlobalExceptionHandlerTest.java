package com.axiang.ax_pro.common;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

public class GlobalExceptionHandlerTest {
    @Test
    void requestNotPermittedReturns429() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        try {
            java.lang.reflect.Constructor<RequestNotPermitted> c = RequestNotPermitted.class.getDeclaredConstructor(String.class, boolean.class);
            c.setAccessible(true);
            RequestNotPermitted ex = c.newInstance("rl", true);
            ResponseEntity<ApiResponse<Object>> resp = h.handleTooManyRequests(ex);
            Assertions.assertEquals(429, resp.getStatusCode().value());
            Assertions.assertEquals("TOO_MANY_REQUESTS", resp.getBody().getCode());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
