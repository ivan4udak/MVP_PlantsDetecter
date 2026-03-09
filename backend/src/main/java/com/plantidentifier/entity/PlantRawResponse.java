package com.plantidentifier.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "plant_raw_responses", schema = "app")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PlantRawResponse {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false) private UUID id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false) private PlantRequest request;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_json", columnDefinition = "jsonb") private String rawJson;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parsed_json", columnDefinition = "jsonb") private String parsedJson;
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