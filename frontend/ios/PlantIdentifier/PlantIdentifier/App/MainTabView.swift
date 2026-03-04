// === File: App/MainTabView.swift ===

import SwiftUI

struct MainTabView: View {
    let apiClient: any APIClientProtocol
    let tokenStorage: any TokenStorageProtocol

    var body: some View {
        TabView {
            IdentifyView(apiClient: apiClient)
                .tabItem { Label("Определить", systemImage: "camera.viewfinder") }

            HistoryView(apiClient: apiClient)
                .tabItem { Label("История", systemImage: "clock") }

            SettingsView(apiClient: apiClient, tokenStorage: tokenStorage)
                .tabItem { Label("Настройки", systemImage: "gearshape") }
        }
    }
}
