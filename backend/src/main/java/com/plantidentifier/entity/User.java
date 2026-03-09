package com.plantidentifier.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "app")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    // ─────────────────────────────────────────────
    // @Id — первичный ключ
    // @GeneratedValue(UUID) — генерирует PostgreSQL
    // updatable = false — нельзя изменить после создания
    // ─────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // unique = true — уникальный индекс в БД
    // NULL разрешён — у гостей нет email
    @Column(name = "email", unique = true, length = 255)
    private String email;

    // Никогда не храним открытый пароль!
    // Только BCrypt хэш: $2a$12$...
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    // ─────────────────────────────────────────────
    // @Enumerated(STRING) — хранит "GUEST"/"REGISTERED"
    // а не 0/1 — так понятнее в БД при дебаге
    // ─────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    @Builder.Default
    private UserType userType = UserType.GUEST;

    // Роль определяет права в Spring Security
    // CustomUserDetails.getAuthorities() возвращает эту роль
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.ROLE_GUEST;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    // Уникальный ID устройства iOS — для привязки гостя
    @Column(name = "device_id", length = 255)
    private String deviceId;

    // Язык ответов AI: "ru", "en", "de"...
    // Кэшируется в JWT claim "lang"
    @Column(name = "preferred_language",
            nullable = false, length = 10)
    @Builder.Default
    private String preferredLanguage = "en";

    // Когда истекает гостевая сессия
    // NULL для REGISTERED пользователей
    @Column(name = "guest_expires_at")
    private LocalDateTime guestExpiresAt;

    // ─────────────────────────────────────────────
    // Audit поля
    // ─────────────────────────────────────────────

    @Column(name = "created_by", length = 100)
    private String createdBy;

    // @CreationTimestamp — Hibernate ставит время
    // автоматически при первом INSERT
    // updatable = false — никогда не меняется
    @CreationTimestamp
    @Column(name = "created_date",
            nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // @UpdateTimestamp — Hibernate обновляет
    // автоматически при каждом UPDATE
    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    // ─────────────────────────────────────────────
    // @Version — оптимистичная блокировка.
    // Hibernate проверяет версию при UPDATE:
    // если версия в БД != версия в памяти → exception
    // Защищает от конкурентных изменений
    // ─────────────────────────────────────────────
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // Soft delete: данные остаются в БД
    // Все запросы фильтруют: WHERE is_deleted = false
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    // ─────────────────────────────────────────────
    // Enums
    // ─────────────────────────────────────────────

    public enum UserType {
        GUEST,       // создан через /session/guest
        REGISTERED   // создан через /auth/register
    }

    // Названия ДОЛЖНЫ начинаться с ROLE_
    // Это требование Spring Security!
    // hasRole("ADMIN") → ищет "ROLE_ADMIN"
    public enum Role {
        ROLE_GUEST,   // 3 запроса/день
        ROLE_USER,    // 30 запросов/день
        ROLE_ADMIN,   // без лимита + /admin/**
        ROLE_SYSTEM   // для внутренних сервисов
    }

    public enum UserStatus {
        ACTIVE,
        BLOCKED,
        DELETED
    }
}