package com.boilerplate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenCacheService {

    private final JedisPool jedisPool;

    public void cacheToken(String email, String token, long expirationInSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex("token:" + email, expirationInSeconds, token);
        }
    }

    public String getToken(String email) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get("token:" + email);
        }
    }

    public void invalidateToken(String email) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("token:" + email);
        }
    }

    public void cacheOtp(String email, String otp) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex("otp:" + email, 300, otp); // 5 minutes
        }
    }

    public String getOtp(String email) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get("otp:" + email);
        }
    }
} 