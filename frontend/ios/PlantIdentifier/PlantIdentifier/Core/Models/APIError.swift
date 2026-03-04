// === File: Core/Models/APIError.swift ===
// Единое место для всех ошибок приложения.
// AppError реализует LocalizedError — SwiftUI умеет показывать .localizedDescription напрямую.

import Foundation

// Серверный error payload (из API-контракта)
struct APIErrorPayload: Decodable {
    let timestamp: String?
    let requestId: String?
    let errorCode: String?
    let message: String?
    let details: String?
}

enum AppError: LocalizedError {
    case network(URLError)
    case httpError(statusCode: Int, payload: APIErrorPayload?)
    case decodingError(DecodingError)
    case unknown(Error)
    case custom(String)

    var errorDescription: String? {
        switch self {
        case .network(let e):
            return "Сетевая ошибка: \(e.localizedDescription)"
        case .httpError(let code, let payload):
            if let msg = payload?.message, !msg.isEmpty {
                return "[\(code)] \(msg)"
            }
            return "HTTP ошибка \(code)"
        case .decodingError(let e):
            return "Ошибка парсинга: \(e.localizedDescription)"
        case .unknown(let e):
            return e.localizedDescription
        case .custom(let msg):
            return msg
        }
    }

    // Код ошибки из payload (для логов/отображения)
    var serverErrorCode: String? {
        if case .httpError(_, let payload) = self {
            return payload?.errorCode
        }
        return nil
    }
}
