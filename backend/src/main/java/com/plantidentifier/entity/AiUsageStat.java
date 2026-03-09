package com.plantidentifier.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Живёт в схеме analytics — не app!
@Entity
@Table(name = "ai_usage_stats", schema = "analytics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiUsageStat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Связь с запросом в схеме app —
    // JPA умеет cross-schema foreign keys
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private PlantRequest request;

    // openai / yandex / gigachat
    @Column(name = "provider", length = 100)
    private String provider;

    // gpt-4.1-mini и т.д.
    @Column(name = "model", length = 100)
    private String model;

    // Сколько токенов потрачено на запрос
    @Column(name = "tokens_used")
    private Integer tokensUsed;

    // Стоимость в USD: до $9999.9999 за запрос
    // NUMERIC(10,4): 10 цифр, 4 после запятой
    @Column(name = "cost_estimate",
            precision = 10, scale = 4)
    private BigDecimal costEstimate;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_date",
            nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}