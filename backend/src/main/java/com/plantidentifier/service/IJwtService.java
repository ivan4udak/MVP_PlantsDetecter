package com.plantidentifier.security;

import com.plantidentifier.entity.User;

import java.util.UUID;

public interface IJwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    UUID extractUserId(String token);
    String extractTokenType(String token);
    boolean validateToken(String token);
    long getAccessTokenExpiration();
}