// === File: App/PlantIdentifierApp.swift ===

import SwiftUI

@main
struct PlantIdentifierApp: App {

    private let tokenStorage = UserDefaultsTokenStorage()

    // ─────────────────────────────────────────
    // 🔧 ПЕРЕКЛЮЧАТЕЛЬ МОК / РЕАЛЬНЫЙ СЕРВЕР
    //   true  → мок-данные (без бэкенда)
    //   false → реальный сервер
    // ─────────────────────────────────────────
    private let useMock = true

    private var apiClient: any APIClientProtocol {
        useMock
            ? MockAPIClient()
            : APIClient(tokenStorage: tokenStorage)
    }

    var body: some Scene {
        WindowGroup {
            BootstrapView(apiClient: apiClient, tokenStorage: tokenStorage)
        }
    }
}
