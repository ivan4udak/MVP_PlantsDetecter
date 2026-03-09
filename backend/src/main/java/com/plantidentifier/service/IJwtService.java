package com.plantidentifier.security;

import com.plantidentifier.entity.User;

import java.util.UUID;

public interface IJwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);

    UUID extractUserId(String token);
    String extractRole(String token);
    String extractLanguage(String token);

    boolean isAccessToken(String token);
    long getAccessTokenExpiration();

    boolean validateToken(String token);
}