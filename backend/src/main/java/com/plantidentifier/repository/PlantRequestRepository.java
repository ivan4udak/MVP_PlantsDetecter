// repository/PlantRequestRepository.java
package com.plantidentifier.repository;

import com.plantidentifier.entity.PlantRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlantRequestRepository extends JpaRepository<PlantRequest, UUID> {

    // История пользователя с пагинацией
    Page<PlantRequest> findByUserIdAndIsDeletedFalseOrderByCreatedDateDesc(
            UUID userId, Pageable pageable
    );

    Optional<PlantRequest> findByIdAndIsDeletedFalse(UUID id);

    // Для rate limiting: считаем запросы за сегодня
    @Query("""
        SELECT COUNT(pr) FROM PlantRequest pr
        WHERE pr.user.id = :userId
          AND pr.isDeleted = false
          AND pr.createdDate >= :since
        """)
    long countByUserIdSince(
            @Param("userId") UUID userId,
            @Param("since") LocalDateTime since
    );

    // Поиск по хэшу изображения (дедупликация)
    Optional<PlantRequest> findByImageHashAndIsDeletedFalse(String imageHash);
}