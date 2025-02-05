package com.server.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final long defaultTtl;

    public RedisService(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${app.redis.default-ttl}") long defaultTtl
    ) {
        this.redisTemplate = redisTemplate;
        this.defaultTtl = defaultTtl;
    }

    /**
     * Store a value with default TTL
     */
    public void set(String key, Object value) {
        set(key, value, defaultTtl);
    }

    /**
     * Store a value as Object with custom TTL
     */
    public void set(String key, Object value, long ttlInSeconds) {
        redisTemplate.opsForValue().set(key, value, ttlInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Get a value by key
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Get a value by key and cast to specific type
     */
    public <T> T get(String key, Class<T> type) {
        Object value = get(key);
        return value != null ? type.cast(value) : null;
    }

    /**
     * Delete a value by key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Check if a key exists
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Update TTL for a key
     */
    public boolean updateTtl(String key, long ttlInSeconds) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, ttlInSeconds, TimeUnit.SECONDS));
    }

    /**
     * Get remaining TTL for a key in seconds
     */
    public long getTtl(String key) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : -1;
    }
}