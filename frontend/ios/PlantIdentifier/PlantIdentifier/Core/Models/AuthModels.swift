// === File: Core/Models/AuthModels.swift ===

import Foundation

// MARK: - Guest Session

struct GuestSessionRequest: Encodable {
    let deviceId: String
    let preferredLanguage: String
}

struct GuestSessionResponse: Decodable {
    let accessToken: String
    let refreshToken: String
    let userId: String?
    let role: String?
    let language: String?
    // techDock также возвращает expiresIn и limitPerDay
    let expiresIn: Int?
    let limitPerDay: Int?
}

// MARK: - Register

struct RegisterRequest: Encodable {
    let email: String
    let password: String
    let language: String
}

// MARK: - Login

struct LoginRequest: Encodable {
    let email: String
    let password: String
}

struct LoginResponse: Decodable {
    let accessToken: String
    let refreshToken: String
}

// MARK: - Refresh Token

struct RefreshTokenRequest: Encodable {
    let refreshToken: String
}

struct RefreshTokenResponse: Decodable {
    let accessToken: String
}

// MARK: - Upgrade Guest

struct UpgradeGuestRequest: Encodable {
    let email: String
    let password: String
}

// MARK: - Update Language

struct UpdateLanguageRequest: Encodable {
    let language: String
}
