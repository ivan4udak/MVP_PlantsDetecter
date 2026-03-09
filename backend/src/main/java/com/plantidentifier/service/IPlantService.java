package com.plantidentifier.service;

import com.plantidentifier.dto.request.PlantAnalyzeRequest;
import com.plantidentifier.dto.response.PlantAnalysisResponse;
import com.plantidentifier.dto.response.PlantHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IPlantService {
    PlantAnalysisResponse analyze(UUID userId, PlantAnalyzeRequest request);
    Page<PlantHistoryResponse> getHistory(UUID userId, Pageable pageable);
    PlantAnalysisResponse getById(UUID id, UUID userId);
    void delete(UUID id, UUID userId);
}