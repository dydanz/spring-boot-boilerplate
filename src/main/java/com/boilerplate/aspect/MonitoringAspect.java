package com.boilerplate.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class MonitoringAspect {

    private final MeterRegistry meterRegistry;

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object monitorEndpoints(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();
        String methodName = joinPoint.getSignature().getName();
        
        try {
            Object result = joinPoint.proceed();
            recordSuccess(methodName, start);
            return result;
        } catch (Exception e) {
            recordError(methodName, e);
            throw e;
        }
    }

    private void recordSuccess(String methodName, Instant start) {
        Duration duration = Duration.between(start, Instant.now());
        meterRegistry.timer("api.request.duration", "method", methodName, "status", "success")
                .record(duration);
        meterRegistry.counter("api.request.count", "method", methodName, "status", "success")
                .increment();
    }

    private void recordError(String methodName, Exception e) {
        meterRegistry.counter("api.request.count", 
                "method", methodName, 
                "status", "error",
                "error", e.getClass().getSimpleName())
                .increment();
    }
} 