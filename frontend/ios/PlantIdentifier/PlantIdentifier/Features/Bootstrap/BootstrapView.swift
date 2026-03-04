// === File: Features/Bootstrap/BootstrapView.swift ===

import SwiftUI

struct BootstrapView: View {
    @StateObject private var vm: BootstrapViewModel

    private let apiClient: any APIClientProtocol
    private let tokenStorage: any TokenStorageProtocol

    init(apiClient: any APIClientProtocol, tokenStorage: any TokenStorageProtocol) {
        self.apiClient = apiClient
        self.tokenStorage = tokenStorage
        _vm = StateObject(wrappedValue: BootstrapViewModel(
            apiClient: apiClient, tokenStorage: tokenStorage))
    }

    var body: some View {
        Group {
            switch vm.state {
            case .idle, .loading:
                LoadingView(message: "Инициализация...")
            case .ready:
                MainTabView(apiClient: apiClient, tokenStorage: tokenStorage)
            case .error(let e):
                ErrorView(error: e) { Task { await vm.bootstrap() } }
            }
        }
        .task { await vm.bootstrap() }
    }
}
