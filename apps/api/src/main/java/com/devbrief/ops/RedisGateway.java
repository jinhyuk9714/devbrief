package com.devbrief.ops;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RedisGateway {
    private final StringRedisTemplate redisTemplate;

    public RedisGateway(ObjectProvider<StringRedisTemplate> redisTemplate) {
        this.redisTemplate = redisTemplate.getIfAvailable();
    }

    public boolean tryLock(String key, Duration ttl) {
        if (redisTemplate == null) {
            return false;
        }
        try {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, "locked", ttl);
            return Boolean.TRUE.equals(locked);
        } catch (Exception ignored) {
            return false;
        }
    }

    public void release(String key) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
        }
    }

    public void put(String key, String value, Duration ttl) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception ignored) {
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        if (redisTemplate == null) {
            status.put("redis", "disabled");
            status.put("hotBriefingCache", false);
            return status;
        }
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            status.put("redis", pong == null ? "unknown" : "available");
            status.put("hotBriefingCache", redisTemplate.hasKey("devbrief:cache:today:last-generated"));
        } catch (Exception ex) {
            status.put("redis", "unavailable");
            status.put("hotBriefingCache", false);
        }
        return status;
    }
}

