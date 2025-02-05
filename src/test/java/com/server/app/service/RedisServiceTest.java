package com.server.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RedisServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private RedisService redisService;

    private static final String TEST_KEY = "test-key";
    private static final String TEST_VALUE = "test-value";
    private static final long DEFAULT_TTL = 3600L; // 1 hour

    @BeforeEach
    void setUp() {
        redisService = new RedisService(redisTemplate, DEFAULT_TTL);
    }

    @Test
    void set_WithDefaultTtl_ShouldSetValueWithDefaultExpiration() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        redisService.set(TEST_KEY, TEST_VALUE);

        // Assert
        verify(valueOperations).set(eq(TEST_KEY), eq(TEST_VALUE), eq(DEFAULT_TTL), eq(TimeUnit.SECONDS));
    }

    @Test
    void set_WithCustomTtl_ShouldSetValueWithCustomExpiration() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        long customTtl = 7200L; // 2 hours

        // Act
        redisService.set(TEST_KEY, TEST_VALUE, customTtl);

        // Assert
        verify(valueOperations).set(eq(TEST_KEY), eq(TEST_VALUE), eq(customTtl), eq(TimeUnit.SECONDS));
    }

    @Test
    void get_WhenKeyExists_ShouldReturnValue() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(TEST_KEY)).thenReturn(TEST_VALUE);

        // Act
        Object result = redisService.get(TEST_KEY);

        // Assert
        assertEquals(TEST_VALUE, result);
        verify(valueOperations).get(TEST_KEY);
    }

    @Test
    void get_WhenKeyDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(TEST_KEY)).thenReturn(null);

        // Act
        Object result = redisService.get(TEST_KEY);

        // Assert
        assertNull(result);
        verify(valueOperations).get(TEST_KEY);
    }

    @Test
    void getWithType_WhenKeyExistsAndTypeMatches_ShouldReturnTypedValue() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(TEST_KEY)).thenReturn(TEST_VALUE);

        // Act
        String result = redisService.get(TEST_KEY, String.class);

        // Assert
        assertEquals(TEST_VALUE, result);
        verify(valueOperations).get(TEST_KEY);
    }

    @Test
    void getWithType_WhenKeyDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(TEST_KEY)).thenReturn(null);

        // Act
        String result = redisService.get(TEST_KEY, String.class);

        // Assert
        assertNull(result);
        verify(valueOperations).get(TEST_KEY);
    }

    @Test
    void delete_ShouldDeleteKey() {
        // Act
        redisService.delete(TEST_KEY);

        // Assert
        verify(redisTemplate).delete(TEST_KEY);
    }

    @Test
    void hasKey_WhenKeyExists_ShouldReturnTrue() {
        // Arrange
        when(redisTemplate.hasKey(TEST_KEY)).thenReturn(true);

        // Act
        boolean result = redisService.hasKey(TEST_KEY);

        // Assert
        assertTrue(result);
        verify(redisTemplate).hasKey(TEST_KEY);
    }

    @Test
    void hasKey_WhenKeyDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(redisTemplate.hasKey(TEST_KEY)).thenReturn(false);

        // Act
        boolean result = redisService.hasKey(TEST_KEY);

        // Assert
        assertFalse(result);
        verify(redisTemplate).hasKey(TEST_KEY);
    }

    @Test
    void updateTtl_WhenSuccessful_ShouldReturnTrue() {
        // Arrange
        when(redisTemplate.expire(eq(TEST_KEY), eq(DEFAULT_TTL), eq(TimeUnit.SECONDS))).thenReturn(true);

        // Act
        boolean result = redisService.updateTtl(TEST_KEY, DEFAULT_TTL);

        // Assert
        assertTrue(result);
        verify(redisTemplate).expire(TEST_KEY, DEFAULT_TTL, TimeUnit.SECONDS);
    }

    @Test
    void updateTtl_WhenFailed_ShouldReturnFalse() {
        // Arrange
        when(redisTemplate.expire(eq(TEST_KEY), eq(DEFAULT_TTL), eq(TimeUnit.SECONDS))).thenReturn(false);

        // Act
        boolean result = redisService.updateTtl(TEST_KEY, DEFAULT_TTL);

        // Assert
        assertFalse(result);
        verify(redisTemplate).expire(TEST_KEY, DEFAULT_TTL, TimeUnit.SECONDS);
    }

    @Test
    void getTtl_WhenKeyExists_ShouldReturnTtl() {
        // Arrange
        when(redisTemplate.getExpire(TEST_KEY, TimeUnit.SECONDS)).thenReturn(DEFAULT_TTL);

        // Act
        long result = redisService.getTtl(TEST_KEY);

        // Assert
        assertEquals(DEFAULT_TTL, result);
        verify(redisTemplate).getExpire(TEST_KEY, TimeUnit.SECONDS);
    }

    @Test
    void getTtl_WhenKeyDoesNotExist_ShouldReturnNegativeOne() {
        // Arrange
        when(redisTemplate.getExpire(TEST_KEY, TimeUnit.SECONDS)).thenReturn(-1L);

        // Act
        long result = redisService.getTtl(TEST_KEY);

        // Assert
        assertEquals(-1L, result);
        verify(redisTemplate).getExpire(TEST_KEY, TimeUnit.SECONDS);
    }
}