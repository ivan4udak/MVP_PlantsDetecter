package com.plantidentifier.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plant_requests", schema = "app")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ─────────────────────────────────────────────
    // @ManyToOne — много запросов у одного юзера
    // FetchType.LAZY — НЕ загружать User из БД
    //   автоматически. Только когда явно вызовем
    //   getUser(). Иначе каждый SELECT plant_requests
    //   будет делать JOIN с users — это лишние запросы
    // @JoinColumn — колонка внешнего ключа
    // ─────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    // SHA-256 хэш URL — для дедупликации запросов
    // Одно фото = один AI запрос
    @Column(name = "image_hash", length = 128)
    private String imageHash;

    // Геолокация — опциональная
    // NUMERIC(9,6): до ±999.999999 градусов
    @Column(name = "latitude",
            precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude",
            precision = 9, scale = 6)
    private BigDecimal longitude;

    // Это вообще растение?
    // false если на фото животное, предмет и т.д.
    @Column(name = "is_plant")
    private Boolean isPlant;

    // Уверенность AI: 0.0000 — 1.0000
    // NUMERIC(5,4): 5 цифр всего, 4 после запятой
    @Column(name = "confidence",
            precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "primary_name", length = 255)
    private String primaryName;

    // Семейство: Betulaceae, Rosaceae...
    @Column(name = "family", length = 255)
    private String family;

    // common / rare / endangered / extinct
    @Column(name = "rarity", length = 100)
    private String rarity;

    @Column(name = "habitat", columnDefinition = "TEXT")
    private String habitat;

    // openai / yandex / gigachat
    @Column(name = "ai_provider", length = 100)
    private String aiProvider;

    // gpt-4.1-mini и т.д.
    @Column(name = "model_name", length = 100)
    private String modelName;

    // Сколько миллисекунд занял AI запрос
    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    // ─────────────────────────────────────────────
    // Audit поля (одинаковые во всех entity)
    // ─────────────────────────────────────────────

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