package com.plantidentifier.service;

import com.plantidentifier.entity.User;

import java.util.UUID;

public interface IRateLimitService {
    void checkLimit(User user, String endpoint);
    void resetLimit(UUID userId);
}