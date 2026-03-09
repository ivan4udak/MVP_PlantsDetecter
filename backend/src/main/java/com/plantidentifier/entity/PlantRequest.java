package com.plantidentifier.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "plant_requests", schema = "app")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PlantRequest {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "image_url", columnDefinition = "TEXT") private String imageUrl;
    @Column(name = "image_hash", length = 128) private String imageHash;
    @Column(name = "latitude", precision = 9, scale = 6) private BigDecimal latitude;
    @Column(name = "longitude", precision = 9, scale = 6) private BigDecimal longitude;
    @Column(name = "is_plant") private Boolean isPlant;
    @Column(name = "confidence", precision = 5, scale = 4) private BigDecimal confidence;
    @Column(name = "primary_name", length = 255) private String primaryName;
    @Column(name = "family", length = 255) private String family;
    @Column(name = "rarity", length = 100) private String rarity;
    @Column(name = "habitat", columnDefinition = "TEXT") private String habitat;
    @Column(name = "ai_provider", length = 100) private String aiProvider;
    @Column(name = "model_name", length = 100) private String modelName;
    @Column(name = "processing_time_ms") private Integer processingTimeMs;
    @Column(name = "created_by", length = 100) private String createdBy;
    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    @Column(name = "updated_by", length = 100) private String updatedBy;
    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false) private LocalDateTime updatedDate;
    @Version @Column(name = "version", nullable = false) private Long version;
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default private Boolean isDeleted = false;
}