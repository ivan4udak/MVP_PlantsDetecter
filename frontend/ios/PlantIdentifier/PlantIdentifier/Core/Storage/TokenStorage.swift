// === File: Core/Storage/TokenStorage.swift ===
// Swift 6: протокол помечен @MainActor чтобы обращение к токенам
// всегда происходило на главном потоке — никаких data races.

import Foundation

@MainActor
protocol TokenStorageProtocol: AnyObject {
    var accessToken: String? { get set }
    var refreshToken: String? { get set }
    var userId: String? { get set }
    var preferredLanguage: String { get set }
    func clearAll()
    func hasTokens() -> Bool
}

@MainActor
final class UserDefaultsTokenStorage: TokenStorageProtocol {

    private enum K {
        static let access   = "auth.accessToken"
        static let refresh  = "auth.refreshToken"
        static let userId   = "auth.userId"
        static let lang     = "auth.preferredLanguage"
    }

    private let d = UserDefaults.standard

    var accessToken: String? {
        get { d.string(forKey: K.access) }
        set { d.set(newValue, forKey: K.access) }
    }
    var refreshToken: String? {
        get { d.string(forKey: K.refresh) }
        set { d.set(newValue, forKey: K.refresh) }
    }
    var userId: String? {
        get { d.string(forKey: K.userId) }
        set { d.set(newValue, forKey: K.userId) }
    }
    var preferredLanguage: String {
        get { d.string(forKey: K.lang) ?? "en" }
        set { d.set(newValue, forKey: K.lang) }
    }

    func clearAll() {
        [K.access, K.refresh, K.userId].forEach { d.removeObject(forKey: $0) }
    }
    func hasTokens() -> Bool { accessToken != nil && refreshToken != nil }
}
