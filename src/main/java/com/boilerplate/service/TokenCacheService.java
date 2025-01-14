package com.boilerplate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void cacheToken(String email, String token, long expirationInSeconds) {
        redisTemplate.opsForValue().set(
            "token:" + email,
            token,
            expirationInSeconds,
            TimeUnit.SECONDS
        );
    }

    public String getToken(String email) {
        Object token = redisTemplate.opsForValue().get("token:" + email);
        return token != null ? token.toString() : null;
    }

    public void invalidateToken(String email) {
        redisTemplate.delete("token:" + email);
    }

    public void cacheOtp(String email, String otp) {
        redisTemplate.opsForValue().set(
            "otp:" + email,
            otp,
            300, // 5 minutes
            TimeUnit.SECONDS
        );
    }

    public String getOtp(String email) {
        Object otp = redisTemplate.opsForValue().get("otp:" + email);
        return otp != null ? otp.toString() : null;
    }
} 