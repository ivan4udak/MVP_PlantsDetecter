// === File: Features/Settings/SettingsView.swift ===

import SwiftUI

struct SettingsView: View {
    @StateObject private var viewModel: SettingsViewModel

    init(apiClient: any APIClientProtocol, tokenStorage: any TokenStorageProtocol) {
        _viewModel = StateObject(wrappedValue: SettingsViewModel(
            apiClient: apiClient,
            tokenStorage: tokenStorage
        ))
    }

    var body: some View {
        NavigationStack {
            Form {
                // MARK: Language
                Section("Язык") {
                    Picker("Язык ответа", selection: $viewModel.selectedLanguage) {
                        Text("Русский").tag("ru")
                        Text("English").tag("en")
                    }
                    .pickerStyle(.segmented)

                    Button {
                        Task { await viewModel.saveLanguage() }
                    } label: {
                        if viewModel.isSaving {
                            ProgressView()
                        } else {
                            Text("Сохранить")
                        }
                    }
                    .disabled(viewModel.isSaving)

                    if viewModel.saveSuccess {
                        Label("Сохранено", systemImage: "checkmark.circle.fill")
                            .foregroundColor(.green)
                    }

                    if let error = viewModel.saveError {
                        Text(error.localizedDescription)
                            .foregroundColor(.red)
                            .font(.caption)
                    }
                }

                // MARK: Account (stub — добавь логин/регистрацию)
                Section("Аккаунт") {
                    NavigationLink("Войти / Зарегистрироваться") {
                        Text("TODO: экран авторизации")
                    }
                    NavigationLink("Обновить до аккаунта") {
                        Text("TODO: экран upgrade guest → user")
                    }
                }

                // MARK: Debug
                Section("Debug") {
                    NavigationLink("Информация о токенах") {
                        TokenDebugView()
                    }
                }
            }
            .navigationTitle("Настройки")
        }
    }
}

// MARK: - Token Debug (удали в продакшне)

private struct TokenDebugView: View {
    private let storage = UserDefaultsTokenStorage()

    var body: some View {
        List {
            LabeledContent("User ID", value: storage.userId ?? "—")
            LabeledContent("Language", value: storage.preferredLanguage)
            LabeledContent("Access Token") {
                Text(storage.accessToken.map { String($0.prefix(20)) + "…" } ?? "—")
                    .font(.caption.monospaced())
            }
            LabeledContent("Refresh Token") {
                Text(storage.refreshToken.map { String($0.prefix(20)) + "…" } ?? "—")
                    .font(.caption.monospaced())
            }
        }
        .navigationTitle("Токены")
    }
}
