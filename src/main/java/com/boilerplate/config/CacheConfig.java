package com.boilerplate.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Configure cache TTLs for different cache names
        cacheConfigurations.put("userProfile", 
            RedisCacheConfiguration.defaultConfig().entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("userAuth", 
            RedisCacheConfiguration.defaultConfig().entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("otpCache", 
            RedisCacheConfiguration.defaultConfig().entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(RedisCacheConfiguration.defaultConfig().entryTtl(Duration.ofMinutes(10)))
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
} 