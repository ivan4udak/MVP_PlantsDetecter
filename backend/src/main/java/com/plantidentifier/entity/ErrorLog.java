package com.plantidentifier.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

// Живёт в схеме audit — только INSERT!
// Нет @Version, нет is_deleted — аудит неизменяем
@Entity
@Table(name = "error_logs", schema = "audit")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Может быть NULL если ошибка до парсинга X-Request-ID
    @Column(name = "request_id")
    private UUID requestId;

    // "RATE_LIMIT_EXCEEDED", "PLANT_NOT_FOUND"...
    @Column(name = "error_code", length = 100)
    private String errorCode;

    // Человекочитаемое сообщение
    @Column(name = "message",
            columnDefinition = "TEXT")
    private String message;

    // Полный stacktrace Java исключения
    @Column(name = "stacktrace",
            columnDefinition = "TEXT")
    private String stacktrace;

    // Только CreationTimestamp — запись не обновляется никогда
    @CreationTimestamp
    @Column(name = "created_date",
            nullable = false, updatable = false)
    private LocalDateTime createdDate;
}
