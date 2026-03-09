// repository/PlantRawResponseRepository.java
package com.plantidentifier.repository;

import com.plantidentifier.entity.PlantRawResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlantRawResponseRepository
        extends JpaRepository<PlantRawResponse, UUID> {

    Optional<PlantRawResponse> findByRequest_Id(UUID requestId);
}