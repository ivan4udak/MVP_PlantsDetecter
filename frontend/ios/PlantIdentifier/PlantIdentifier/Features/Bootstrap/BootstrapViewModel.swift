// === File: Features/Bootstrap/BootstrapViewModel.swift ===
// Swift 6 / Xcode 26: весь класс помечен @MainActor — тогда
// ObservableObject синтезируется корректно и все @Published обновляются на главном потоке.

import Foundation
import Combine

@MainActor
final class BootstrapViewModel: ObservableObject {

    enum BootstrapState {
        case idle, loading, ready
        case error(AppError)
    }

    @Published private(set) var state: BootstrapState = .idle

    private let apiClient: any APIClientProtocol
    private let tokenStorage: any TokenStorageProtocol

    init(apiClient: any APIClientProtocol, tokenStorage: any TokenStorageProtocol) {
        self.apiClient = apiClient
        self.tokenStorage = tokenStorage
    }

    func bootstrap() async {
        if tokenStorage.hasTokens() { state = .ready; return }
        state = .loading
        do {
            let response = try await apiClient.createGuestSession(
                deviceId: DeviceIdentifier.current(),
                language: tokenStorage.preferredLanguage
            )
            tokenStorage.accessToken  = response.accessToken
            tokenStorage.refreshToken = response.refreshToken
            if let id   = response.userId    { tokenStorage.userId = id }
            if let lang = response.language  { tokenStorage.preferredLanguage = lang }
            state = .ready
        } catch let e as AppError { state = .error(e)
        } catch { state = .error(.unknown(error)) }
    }
}

enum DeviceIdentifier {
    static func current() -> String {
        let key = "app.deviceId"
        if let v = UserDefaults.standard.string(forKey: key) { return v }
        let v = "ios-\(UUID().uuidString)"
        UserDefaults.standard.set(v, forKey: key)
        return v
    }
}
