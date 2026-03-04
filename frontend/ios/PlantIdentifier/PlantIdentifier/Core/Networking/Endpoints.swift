// === File: Core/Networking/Endpoints.swift ===
// Default-значения параметров задаются здесь, а не в протоколе.
// Протокол содержит только сигнатуры без defaults — это требование Swift.

import Foundation

extension APIClientProtocol {

    // MARK: - Удобные обёртки с default-параметрами

    func get<T: Decodable>(_ endpoint: String) async throws -> T {
        try await request(endpoint: endpoint, method: "GET", body: nil, requiresAuth: true)
    }

    func post<T: Decodable>(_ endpoint: String, body: any Encodable, auth: Bool = true) async throws -> T {
        try await request(endpoint: endpoint, method: "POST", body: body, requiresAuth: auth)
    }

    func patch(_ endpoint: String, body: any Encodable) async throws {
        try await requestVoid(endpoint: endpoint, method: "PATCH", body: body, requiresAuth: true)
    }

    func delete(_ endpoint: String) async throws {
        try await requestVoid(endpoint: endpoint, method: "DELETE", body: nil, requiresAuth: true)
    }

    // MARK: - Auth & Session

    func createGuestSession(deviceId: String, language: String) async throws -> GuestSessionResponse {
        try await request(
            endpoint: "/session/guest", method: "POST",
            body: GuestSessionRequest(deviceId: deviceId, preferredLanguage: language),
            requiresAuth: false)
    }

    func login(email: String, password: String) async throws -> LoginResponse {
        try await request(
            endpoint: "/auth/login", method: "POST",
            body: LoginRequest(email: email, password: password),
            requiresAuth: false)
    }

    func register(email: String, password: String, language: String) async throws {
        try await requestVoid(
            endpoint: "/auth/register", method: "POST",
            body: RegisterRequest(email: email, password: password, language: language),
            requiresAuth: false)
    }

    func refreshToken(_ token: String) async throws -> RefreshTokenResponse {
        try await request(
            endpoint: "/auth/refresh", method: "POST",
            body: RefreshTokenRequest(refreshToken: token),
            requiresAuth: false)
    }

    func upgradeGuest(email: String, password: String) async throws {
        try await requestVoid(
            endpoint: "/auth/upgrade", method: "POST",
            body: UpgradeGuestRequest(email: email, password: password),
            requiresAuth: true)
    }

    func updateLanguage(_ language: String) async throws {
        try await requestVoid(
            endpoint: "/users/language", method: "PATCH",
            body: UpdateLanguageRequest(language: language),
            requiresAuth: true)
    }

    // MARK: - Plants

    func analyzePlant(mode: AnalyzeRequestMode) async throws -> AnalyzeResponse {
        switch mode {
        case .jsonUrl(let imageUrl, let lat, let lng):
            var meta: [String: String] = [:]
            if let lat { meta["lat"] = String(lat) }
            if let lng { meta["lng"] = String(lng) }
            return try await request(
                endpoint: "/plants/analyze", method: "POST",
                body: AnalyzeRequestBody(imageUrl: imageUrl, clientMetadata: meta.isEmpty ? nil : meta),
                requiresAuth: true)

        case .multipart(let imageData, let mimeType, let lat, let lng):
            var fields: [String: String] = [:]
            if let lat { fields["lat"] = String(lat) }
            if let lng { fields["lng"] = String(lng) }
            return try await multipartRequest(
                endpoint: "/plants/analyze",
                imageData: imageData,
                mimeType: mimeType,
                fileName: "photo.jpg",
                additionalFields: fields)
        }
    }

    func fetchHistory(page: Int = 0, size: Int = 20) async throws -> [PlantHistoryItem] {
        let ep = "/plants/history?page=\(page)&size=\(size)"
        do {
            return try await request(endpoint: ep, method: "GET", body: nil, requiresAuth: true)
        } catch AppError.decodingError {
            let paged: PlantHistoryResponse = try await request(endpoint: ep, method: "GET", body: nil, requiresAuth: true)
            return paged.content ?? []
        }
    }

    func fetchPlant(id: String) async throws -> AnalyzeResponse {
        try await request(endpoint: "/plants/\(id)", method: "GET", body: nil, requiresAuth: true)
    }

    func deletePlant(id: String) async throws {
        try await requestVoid(endpoint: "/plants/\(id)", method: "DELETE", body: nil, requiresAuth: true)
    }
}
