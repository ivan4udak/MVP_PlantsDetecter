package com.plantidentifier.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "users", schema = "app")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "email", unique = true, length = 255)
    private String email;
    @Column(name = "password_hash", length = 255)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    @Builder.Default private UserType userType = UserType.GUEST;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default private Role role = Role.ROLE_GUEST;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default private UserStatus status = UserStatus.ACTIVE;
    @Column(name = "device_id", length = 255)
    private String deviceId;
    @Column(name = "preferred_language", nullable = false, length = 10)
    @Builder.Default private String preferredLanguage = "en";
    @Column(name = "guest_expires_at")
    private LocalDateTime guestExpiresAt;
    @Column(name = "created_by", length = 100) private String createdBy;
    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    @Column(name = "updated_by", length = 100) private String updatedBy;
    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;
    @Version @Column(name = "version", nullable = false) private Long version;
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default private Boolean isDeleted = false;

    public enum UserType { GUEST, REGISTERED }
    public enum Role { ROLE_GUEST, ROLE_USER, ROLE_ADMIN, ROLE_SYSTEM }
    public enum UserStatus { ACTIVE, BLOCKED, DELETED }
}