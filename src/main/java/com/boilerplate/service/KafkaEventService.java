package com.boilerplate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void trackUserEvent(Long userId, String eventName) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("userId", userId);
            event.put("eventName", eventName);
            event.put("timestamp", LocalDateTime.now().toString());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("user-events", userId.toString(), message);
        } catch (Exception e) {
            log.error("Failed to track user event: {}", e.getMessage());
        }
    }
} 