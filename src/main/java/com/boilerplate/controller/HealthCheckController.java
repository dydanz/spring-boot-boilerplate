package com.boilerplate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.core.env.Environment;
import javax.sql.DataSource;
import redis.clients.jedis.Jedis;
import lombok.RequiredArgsConstructor;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthCheckController {

    private final DataSource dataSource;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Environment environment;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        Map<String, Object> components = new HashMap<>();

        // Check Database
        try {
            dataSource.getConnection();
            components.put("database", Map.of(
                "status", "UP",
                "message", "Database connection is healthy"
            ));
        } catch (Exception e) {
            components.put("database", Map.of(
                "status", "DOWN",
                "message", e.getMessage()
            ));
        }

        // Check Redis
        try {
            Jedis jedis = new Jedis(
                environment.getProperty("spring.redis.host", "localhost"),
                environment.getProperty("spring.redis.port", Integer.class, 6379)
            );
            jedis.auth(environment.getProperty("spring.redis.password"));
            String response = jedis.ping();
            jedis.close();
            components.put("redis", Map.of(
                "status", "UP",
                "message", "Redis connection is healthy",
                "response", response
            ));
        } catch (Exception e) {
            components.put("redis", Map.of(
                "status", "DOWN",
                "message", e.getMessage()
            ));
        }

        // Check Kafka
        try {
            kafkaTemplate.getDefaultTopic();
            components.put("kafka", Map.of(
                "status", "UP",
                "message", "Kafka connection is healthy"
            ));
        } catch (Exception e) {
            components.put("kafka", Map.of(
                "status", "DOWN",
                "message", e.getMessage()
            ));
        }

        status.put("status", components.values().stream().allMatch(c -> ((Map<String, String>) c).get("status").equals("UP")) ? "UP" : "DOWN");
        status.put("components", components);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Pong");
    }
}