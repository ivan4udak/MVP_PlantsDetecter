// === File: Features/Settings/SettingsViewModel.swift ===

import Foundation
import Combine

@MainActor
final class SettingsViewModel: ObservableObject {

    @Published var selectedLanguage: String
    @Published private(set) var isSaving = false
    @Published private(set) var saveSuccess = false
    @Published private(set) var saveError: AppError?

    private let apiClient: any APIClientProtocol
    private let tokenStorage: any TokenStorageProtocol

    init(apiClient: any APIClientProtocol, tokenStorage: any TokenStorageProtocol) {
        self.apiClient = apiClient
        self.tokenStorage = tokenStorage
        self.selectedLanguage = tokenStorage.preferredLanguage
    }

    func saveLanguage() async {
        isSaving = true; saveError = nil; saveSuccess = false
        do {
            try await apiClient.updateLanguage(selectedLanguage)
            tokenStorage.preferredLanguage = selectedLanguage
            saveSuccess = true
        } catch let e as AppError { saveError = e
        } catch { saveError = .unknown(error) }
        isSaving = false
    }
}
