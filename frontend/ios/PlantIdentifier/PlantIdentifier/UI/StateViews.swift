// === File: UI/StateViews.swift ===
// Переиспользуемые компоненты состояний.
// Используй их в любом экране: LoadingView(), ErrorView(error:retry:), EmptyView(message:)

import SwiftUI

// MARK: - Loading

struct LoadingView: View {
    var message: String = "Загрузка..."

    var body: some View {
        VStack(spacing: 16) {
            ProgressView()
                .scaleEffect(1.4)
            Text(message)
                .foregroundColor(.secondary)
                .font(.subheadline)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

// MARK: - Error

struct ErrorView: View {
    let error: Error
    let retry: (() -> Void)?

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 48))
                .foregroundColor(.orange)

            Text(error.localizedDescription)
                .multilineTextAlignment(.center)
                .foregroundColor(.primary)
                .padding(.horizontal, 32)

            if let retry = retry {
                Button(action: retry) {
                    Label("Повторить", systemImage: "arrow.clockwise")
                        .padding(.horizontal, 24)
                        .padding(.vertical, 10)
                        .background(Color.accentColor)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

// MARK: - Empty State

struct EmptyStateView: View {
    var icon: String = "leaf"
    var message: String = "Ничего не найдено"

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 48))
                .foregroundColor(.secondary)
            Text(message)
                .foregroundColor(.secondary)
                .font(.subheadline)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

// MARK: - Previews

#Preview("Loading") { LoadingView() }
#Preview("Error") {
    ErrorView(error: AppError.custom("Сервер недоступен")) {
        print("Retry tapped")
    }
}
#Preview("Empty") { EmptyStateView() }
