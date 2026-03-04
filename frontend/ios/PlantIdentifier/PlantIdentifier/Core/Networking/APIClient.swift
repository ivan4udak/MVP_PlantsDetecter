// === File: Core/Networking/APIClient.swift ===

import Foundation

// MARK: - Protocol

protocol APIClientProtocol {
    func request<T: Decodable>(endpoint: String, method: String, body: (any Encodable)?, requiresAuth: Bool) async throws -> T
    func requestVoid(endpoint: String, method: String, body: (any Encodable)?, requiresAuth: Bool) async throws
    func multipartRequest<T: Decodable>(endpoint: String, imageData: Data, mimeType: String, fileName: String, additionalFields: [String: String]) async throws -> T
}

// MARK: - Real APIClient

final class APIClient: APIClientProtocol {

    // ⚠️ УКАЖИ СВОЙ baseURL здесь
    static let baseURL = "https://YOUR_API_HOST/api/v1"

    private let session: URLSession
    nonisolated(unsafe) private let tokenStorage: any TokenStorageProtocol

    init(tokenStorage: any TokenStorageProtocol, session: URLSession = .shared) {
        self.tokenStorage = tokenStorage
        self.session = session
    }

    func request<T: Decodable>(
        endpoint: String,
        method: String,
        body: (any Encodable)?,
        requiresAuth: Bool
    ) async throws -> T {
        let data = try await rawRequest(endpoint: endpoint, method: method, body: body, requiresAuth: requiresAuth)
        do {
            return try JSONDecoder().decode(T.self, from: data)
        } catch let e as DecodingError { throw AppError.decodingError(e) }
    }

    func requestVoid(
        endpoint: String,
        method: String,
        body: (any Encodable)?,
        requiresAuth: Bool
    ) async throws {
        _ = try await rawRequest(endpoint: endpoint, method: method, body: body, requiresAuth: requiresAuth)
    }

    func multipartRequest<T: Decodable>(
        endpoint: String,
        imageData: Data,
        mimeType: String,
        fileName: String,
        additionalFields: [String: String]
    ) async throws -> T {
        guard let url = URL(string: Self.baseURL + endpoint) else {
            throw AppError.custom("Неверный URL")
        }
        let (token, lang) = await MainActor.run {
            (tokenStorage.accessToken, tokenStorage.preferredLanguage)
        }
        let boundary = "Boundary-\(UUID().uuidString)"
        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        applyHeaders(to: &req, contentType: "multipart/form-data; boundary=\(boundary)", token: token, lang: lang)
        req.httpBody = buildMultipart(boundary: boundary, imageData: imageData, mimeType: mimeType, fileName: fileName, fields: additionalFields)
        let data = try await perform(req)
        do {
            return try JSONDecoder().decode(T.self, from: data)
        } catch let e as DecodingError { throw AppError.decodingError(e) }
    }

    private func rawRequest(endpoint: String, method: String, body: (any Encodable)?, requiresAuth: Bool) async throws -> Data {
        guard let url = URL(string: Self.baseURL + endpoint) else {
            throw AppError.custom("Неверный URL: \(endpoint)")
        }
        let (token, lang) = await MainActor.run {
            (tokenStorage.accessToken, tokenStorage.preferredLanguage)
        }
        var req = URLRequest(url: url)
        req.httpMethod = method
        applyHeaders(to: &req, contentType: body != nil ? "application/json" : nil, token: requiresAuth ? token : nil, lang: lang)
        if let body { req.httpBody = try JSONEncoder().encode(body) }
        return try await perform(req)
    }

    private func perform(_ req: URLRequest) async throws -> Data {
        do {
            let (data, response) = try await session.data(for: req)
            guard let http = response as? HTTPURLResponse else { throw AppError.custom("Не HTTP ответ") }
            guard (200...299).contains(http.statusCode) else {
                let payload = try? JSONDecoder().decode(APIErrorPayload.self, from: data)
                throw AppError.httpError(statusCode: http.statusCode, payload: payload)
            }
            return data
        } catch let e as AppError { throw e
        } catch let e as URLError { throw AppError.network(e)
        } catch { throw AppError.unknown(error) }
    }

    private func applyHeaders(to req: inout URLRequest, contentType: String?, token: String?, lang: String) {
        req.setValue(UUID().uuidString,  forHTTPHeaderField: "X-Request-ID")
        req.setValue("application/json", forHTTPHeaderField: "Accept")
        req.setValue(lang,               forHTTPHeaderField: "Accept-Language")
        if let ct = contentType { req.setValue(ct,            forHTTPHeaderField: "Content-Type") }
        if let t  = token       { req.setValue("Bearer \(t)", forHTTPHeaderField: "Authorization") }
    }

    private func buildMultipart(boundary: String, imageData: Data, mimeType: String, fileName: String, fields: [String: String]) -> Data {
        var body = Data()
        let crlf = "\r\n"
        for (k, v) in fields {
            body.appendStr("--\(boundary)\(crlf)Content-Disposition: form-data; name=\"\(k)\"\(crlf)\(crlf)\(v)\(crlf)")
        }
        body.appendStr("--\(boundary)\(crlf)")
        body.appendStr("Content-Disposition: form-data; name=\"image\"; filename=\"\(fileName)\"\(crlf)")
        body.appendStr("Content-Type: \(mimeType)\(crlf)\(crlf)")
        body.append(imageData)
        body.appendStr("\(crlf)--\(boundary)--\(crlf)")
        return body
    }
}

private extension Data {
    mutating func appendStr(_ s: String) {
        if let d = s.data(using: .utf8) { append(d) }
    }
}
