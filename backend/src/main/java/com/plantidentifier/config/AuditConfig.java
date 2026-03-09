// config/AuditConfig.java
package com.plantidentifier.config;

import com.plantidentifier.util.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Конфигурация JPA аудита.
 *
 * @EnableJpaAuditing — включает автоматическое заполнение
 * @CreatedBy, @LastModifiedBy в Entity классах.
 * (Мы не используем эти аннотации, но конфиг полезен на будущее)
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    /**
     * AuditorAware — Spring Data спрашивает:
     * "Кто сейчас делает изменения?"
     * Мы отвечаем: текущий userId из SecurityContext.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                return Optional.of(
                        SecurityUtils.getCurrentUserId().toString()
                );
            } catch (Exception e) {
                // Если нет аутентифицированного пользователя
                // (например при Flyway миграциях)
                return Optional.of("system");
            }
        };
    }
}