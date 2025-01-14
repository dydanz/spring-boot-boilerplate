package com.boilerplate.performance;

import com.boilerplate.dto.LoginRequestDto;
import com.boilerplate.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthenticationPerformanceTest {

    @Autowired
    private AuthService authService;

    @Test
    void testConcurrentLogins() throws InterruptedException {
        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        Instant start = Instant.now();

        for (int i = 0; i < numberOfThreads; i++) {
            final int userIndex = i;
            executorService.submit(() -> {
                try {
                    LoginRequestDto request = new LoginRequestDto();
                    request.setEmail("test" + userIndex + "@example.com");
                    request.setPassword("password123");
                    authService.login(request);
                } catch (Exception e) {
                    // Log exception
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        assertTrue(completed, "Performance test did not complete in time");
        assertTrue(duration.getSeconds() < 10, "Performance test took too long");
    }
} 