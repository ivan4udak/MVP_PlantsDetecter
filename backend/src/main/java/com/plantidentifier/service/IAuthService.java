package com.plantidentifier.service;

import com.plantidentifier.dto.request.LoginRequest;
import com.plantidentifier.dto.request.RefreshTokenRequest;
import com.plantidentifier.dto.request.RegisterRequest;
import com.plantidentifier.dto.request.UpgradeRequest;
import com.plantidentifier.dto.response.AuthResponse;

import java.util.UUID;

public interface IAuthService {
    AuthResponse.RegisterResponse register(RegisterRequest request);
    AuthResponse.TokenPair login(LoginRequest request);
    AuthResponse.AccessTokenResponse refresh(RefreshTokenRequest request);
    void upgrade(UUID guestUserId, UpgradeRequest request);
}