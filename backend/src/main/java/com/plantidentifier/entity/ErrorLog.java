package com.plantidentifier.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "error_logs", schema = "audit")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ErrorLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false) private UUID id;
    @Column(name = "request_id") private UUID requestId;
    @Column(name = "error_code", length = 100) private String errorCode;
    @Column(name = "message", columnDefinition = "TEXT") private String message;
    @Column(name = "stacktrace", columnDefinition = "TEXT") private String stacktrace;
    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
}