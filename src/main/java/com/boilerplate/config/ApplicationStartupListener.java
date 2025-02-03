package com.boilerplate.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import org.springframework.core.env.Environment;
import redis.clients.jedis.Jedis;

@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationStartedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);
    
    private final DataSource dataSource;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Environment environment;

    public ApplicationStartupListener(DataSource dataSource, 
                                    KafkaTemplate<String, String> kafkaTemplate,
                                    Environment environment) {
        this.dataSource = dataSource;
        this.kafkaTemplate = kafkaTemplate;
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        checkDatabaseConnection();
        checkRedisConnection();
        checkKafkaConnection();
        logger.info("All components initialized successfully");
    }

    private void checkDatabaseConnection() {
        try {
            dataSource.getConnection();
            logger.info("✅ Database connection established successfully");
        } catch (Exception e) {
            logger.error("❌ Failed to connect to database: {}", e.getMessage());
        }
    }

    private void checkRedisConnection() {
        try {
            Jedis jedis = new Jedis(
                environment.getProperty("spring.redis.host", "localhost"),
                environment.getProperty("spring.redis.port", Integer.class, 6379)
            );
            jedis.auth(environment.getProperty("spring.redis.password"));
            
            String response = jedis.ping();
            jedis.close();
            logger.info("✅ Redis connection established successfully: {}", response);
        } catch (Exception e) {
            logger.error("❌ Failed to connect to Redis: {}", e.getMessage());
        }
    }

    private void checkKafkaConnection() {
        try {
            kafkaTemplate.getDefaultTopic();
            logger.info("✅ Kafka connection initialized successfully");
        } catch (Exception e) {
            logger.error("❌ Failed to initialize Kafka: {}", e.getMessage());
        }
    }
} 