package com.plantidentifier.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "rate_limit_log", schema = "analytics")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RateLimitLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "endpoint", length = 100) private String endpoint;
    @Column(name = "request_count") private Integer requestCount;
    @Column(name = "limit_value") private Integer limitValue;
    @Column(name = "blocked", nullable = false)
    @Builder.Default private Boolean blocked = false;
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