// === File: Core/Models/PlantModels.swift ===

import Foundation

// MARK: - Analyze Request
// TODO: уточнить финальный контракт — multipart/form-data (image file) vs JSON (imageUrl).
// Сейчас поддерживаем оба варианта через enum:

enum AnalyzeRequestMode {
    case jsonUrl(imageUrl: String, lat: Double?, lng: Double?)
    case multipart(imageData: Data, mimeType: String, lat: Double?, lng: Double?)
}

// JSON-вариант тела
struct AnalyzeRequestBody: Encodable {
    let imageUrl: String
    let clientMetadata: [String: String]?
}

// MARK: - Analyze Response

struct PlantAlternative: Decodable {
    let name: String
    let confidence: Double
}

struct PlantPrimaryResult: Decodable {
    let name: String?
    let family: String?
    let rarity: String?
    let habitat: String?
    let facts: String?
    // Specification.txt использует plantName/latinName на верхнем уровне
}

struct AnalyzeResponse: Decodable {
    let requestId: String
    // techDock.txt top-level fields
    let isPlant: Bool?
    let confidence: Double?
    let primaryResult: PlantPrimaryResult?
    let alternatives: [PlantAlternative]?
    // Specification.txt top-level fields
    let plantName: String?
    let latinName: String?
    let description: String?
    // model info
    let processingTimeMs: Int?

    // Удобный accessor независимо от варианта контракта
    var displayName: String {
        primaryResult?.name ?? plantName ?? latinName ?? "Неизвестное растение"
    }
}

// MARK: - History

struct PlantHistoryItem: Decodable, Identifiable {
    let requestId: String
    let primaryName: String?
    let plantName: String?   // Specification.txt
    let confidence: Double?
    let createdDate: String?

    var id: String { requestId }
    var displayName: String { primaryName ?? plantName ?? "—" }
}

struct PlantHistoryResponse: Decodable {
    let content: [PlantHistoryItem]?   // если сервер оборачивает в Page
    // или сервер может вернуть просто массив — обрабатываем в APIClient
}
