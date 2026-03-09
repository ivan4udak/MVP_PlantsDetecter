package com.plantidentifier.service;

import java.util.UUID;

public interface IAuditService {
    void logError(UUID requestId, String errorCode, String message, String stacktrace);
    void logSystemEvent(String eventType, String severity, Object payload);
}