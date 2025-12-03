package com.axiang.ax_pro.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
/**
 * 统计服务
 * 使用 Redis 记录 PV、UV，并维护测评访问排行。
 */
public class StatsService {
    private final RedisTemplate<String, String> redisTemplate;

    public StatsService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 增加页面访问量（PV）并更新排行榜分值
     */
    public void incrPv(Long testId) {
        String key = "test:pv:" + testId;
        try {
            redisTemplate.opsForValue().increment(key);
            redisTemplate.opsForZSet().incrementScore("test:rank", String.valueOf(testId), 1);
        } catch (Exception ignored) {}
    }

    /**
     * 记录唯一访客（UV）
     */
    public void recordUv(Long testId, String userId) {
        String key = "test:uv:" + testId;
        try {
            redisTemplate.opsForSet().add(key, userId);
        } catch (Exception ignored) {}
    }
}

