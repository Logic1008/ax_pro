package com.axiang.ax_pro.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
/**
 * 统计服务
 * - 记录 PV/UV 并维护排行
 * - 通过 Resilience4j 提供熔断与降级，异常时不影响主流程
 * - 使用 Micrometer 记录失败计数，便于监控与告警
 */
public class StatsService {
    private static final Logger logger = LoggerFactory.getLogger(StatsService.class);
    private static final String KEY_PV_PREFIX = "test:pv:";
    private static final String KEY_UV_PREFIX = "test:uv:";
    private static final String KEY_RANK = "test:rank";

    private final RedisTemplate<String, String> redisTemplate;
    private final Counter redisFailCounter;

    /**
     * 注入 RedisTemplate 与度量注册器，构建失败计数器
     */
    public StatsService(RedisTemplate<String, String> redisTemplate, MeterRegistry registry) {
        this.redisTemplate = redisTemplate;
        this.redisFailCounter = registry.counter("stats.redis.fail");
    }

    /**
     * 增加 PV 并提升排行分值
     * 失败时触发熔断策略并回落到降级方法
     */
    @CircuitBreaker(name = "redis", fallbackMethod = "incrPvFallback")
    public void incrPv(Long testId) {
        String key = KEY_PV_PREFIX + testId;
        try {
            redisTemplate.opsForValue().increment(key);
            redisTemplate.opsForZSet().incrementScore(KEY_RANK, String.valueOf(testId), 1);
        } catch (Exception e) {
            logger.warn("redis pv incr failed: {}", testId, e);
            redisFailCounter.increment();
        }
    }

    /** PV 降级：记录失败并输出警告日志 */
    public void incrPvFallback(Long testId, Throwable t) {
        logger.warn("pv fallback: {}", testId, t);
        redisFailCounter.increment();
    }

    /**
     * 记录 UV（按用户维度）
     * 失败时触发熔断策略并回落到降级方法
     */
    @CircuitBreaker(name = "redis", fallbackMethod = "recordUvFallback")
    public void recordUv(Long testId, String userId) {
        String key = KEY_UV_PREFIX + testId;
        try {
            redisTemplate.opsForSet().add(key, userId);
        } catch (Exception e) {
            logger.warn("redis uv record failed: {} {}", testId, userId, e);
            redisFailCounter.increment();
        }
    }

    /** UV 降级：记录失败并输出警告日志 */
    public void recordUvFallback(Long testId, String userId, Throwable t) {
        logger.warn("uv fallback: {} {}", testId, userId, t);
        redisFailCounter.increment();
    }
}

