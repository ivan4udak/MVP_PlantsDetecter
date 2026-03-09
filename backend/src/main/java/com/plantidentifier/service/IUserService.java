package com.plantidentifier.service;

import com.plantidentifier.dto.request.LanguageUpdateRequest;

import java.util.UUID;

public interface IUserService {
    void updateLanguage(UUID userId, LanguageUpdateRequest request);
}