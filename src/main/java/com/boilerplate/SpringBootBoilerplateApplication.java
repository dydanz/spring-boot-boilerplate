package com.boilerplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EntityScan("com.boilerplate.entity")
@EnableJpaRepositories("com.boilerplate.repository")
public class SpringBootBoilerplateApplication {
    private static final Logger logger = LoggerFactory.getLogger(SpringBootBoilerplateApplication.class);

    public static void main(String[] args) {
        logger.info("🚀 Starting Spring Boot Boilerplate Application...");
        SpringApplication.run(SpringBootBoilerplateApplication.class, args);
    }

    @EventListener(ApplicationStartingEvent.class)
    public void onStart() {
        logger.info("⚙️ Initializing application components...");
    }
} 