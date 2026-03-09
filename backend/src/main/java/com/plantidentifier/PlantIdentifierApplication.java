package com.plantidentifier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication
// @EnableAsync — включает асинхронное выполнение для @Async методов
// в AuditService (logError, logSystemEvent работают в отдельном потоке)
@EnableAsync
public class PlantIdentifierApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlantIdentifierApplication.class, args);
        log.info("""
            
            ================================
            🌿 Plant Identifier Backend
            ================================
            API: http://localhost:8080/api/v1
            Health: http://localhost:8080/api/v1/system/health
            pgAdmin: http://localhost:5050
            ================================
            """);
    }
}