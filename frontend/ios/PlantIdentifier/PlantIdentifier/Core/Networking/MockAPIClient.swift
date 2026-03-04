// === File: Core/Networking/MockAPIClient.swift ===
// Реализует APIClientProtocol напрямую — без наследования.

import Foundation

final class MockAPIClient: APIClientProtocol {

    private func delay() async throws {
        try await Task.sleep(nanoseconds: 800_000_000)
    }

    func request<T: Decodable>(endpoint: String, method: String, body: (any Encodable)?, requiresAuth: Bool) async throws -> T {
        try await delay()
        return try mockResponse(for: endpoint, method: method)
    }

    func requestVoid(endpoint: String, method: String, body: (any Encodable)?, requiresAuth: Bool) async throws {
        try await delay()
    }

    func multipartRequest<T: Decodable>(endpoint: String, imageData: Data, mimeType: String, fileName: String, additionalFields: [String: String]) async throws -> T {
        try await delay()
        return try mockResponse(for: "/plants/analyze", method: "POST")
    }

    private func mockResponse<T: Decodable>(for endpoint: String, method: String) throws -> T {
        let json: String
        if endpoint.contains("/session/guest") {
            json = """
            {"accessToken":"mock-access","refreshToken":"mock-refresh",
             "userId":"mock-uid","role":"ROLE_GUEST","language":"ru",
             "expiresIn":86400,"limitPerDay":3}
            """
        } else if endpoint.contains("/plants/analyze") {
            json = """
            {"requestId":"mock-001","isPlant":true,"confidence":0.94,
             "plantName":"Берёза повислая","latinName":"Betula pendula",
             "description":"Листопадное дерево семейства Берёзовые. Широко распространена в России.",
             "primaryResult":{"name":"Betula pendula","family":"Betulaceae",
               "rarity":"common","habitat":"Европа, Западная Сибирь",
               "facts":"Живёт до 150 лет, достигает высоты 30 м."},
             "alternatives":[{"name":"Betula pubescens","confidence":0.43}],
             "processingTimeMs":1240}
            """
        } else if endpoint.contains("/plants/history") {
            json = """
            [{"requestId":"mock-001","primaryName":"Берёза повислая","plantName":"Берёза повислая","confidence":0.94,"createdDate":"2026-03-04T18:00:00Z"},
             {"requestId":"mock-002","primaryName":"Дуб черешчатый","plantName":"Дуб черешчатый","confidence":0.88,"createdDate":"2026-03-03T14:30:00Z"},
             {"requestId":"mock-003","primaryName":"Сосна обыкновенная","plantName":"Сосна обыкновенная","confidence":0.76,"createdDate":"2026-03-02T10:15:00Z"}]
            """
        } else if endpoint.contains("/plants/") {
            json = """
            {"requestId":"mock-001","isPlant":true,"confidence":0.94,
             "plantName":"Берёза повислая","latinName":"Betula pendula",
             "description":"Листопадное дерево семейства Берёзовые.","processingTimeMs":1240}
            """
        } else {
            json = "{}"
        }
        guard let data = json.data(using: .utf8) else { throw AppError.custom("Mock: ошибка данных") }
        do {
            return try JSONDecoder().decode(T.self, from: data)
        } catch let e as DecodingError { throw AppError.decodingError(e) }
    }
}
