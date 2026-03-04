// === File: Features/Identify/IdentifyView.swift ===

import SwiftUI
import PhotosUI

struct IdentifyView: View {
    @StateObject private var viewModel: IdentifyViewModel
    @State private var photoPickerItem: PhotosPickerItem?

    init(apiClient: any APIClientProtocol) {
        _viewModel = StateObject(wrappedValue: IdentifyViewModel(apiClient: apiClient))
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {

                PhotosPicker(selection: $photoPickerItem, matching: .images) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 16)
                            .fill(Color(.secondarySystemBackground))
                            .frame(height: 260)

                        if let img = viewModel.selectedImage {
                            Image(uiImage: img)
                                .resizable()
                                .scaledToFill()
                                .frame(height: 260)
                                .clipShape(RoundedRectangle(cornerRadius: 16))
                        } else {
                            VStack(spacing: 12) {
                                Image(systemName: "camera.fill")
                                    .font(.system(size: 44))
                                    .foregroundColor(.accentColor)
                                Text("Нажмите, чтобы выбрать фото")
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                }
                .onChange(of: photoPickerItem) { _, newItem in
                    Task { @MainActor in
                        if let data = try? await newItem?.loadTransferable(type: Data.self),
                           let uiImage = UIImage(data: data) {
                            viewModel.selectedImage = uiImage
                        }
                    }
                }

                switch viewModel.state {
                case .idle:
                    Button("Определить растение") {
                        Task { await viewModel.analyze() }
                    }
                    .buttonStyle(.borderedProminent)
                    .disabled(viewModel.selectedImage == nil)

                case .loading:
                    LoadingView(message: "Анализируем...").frame(height: 100)

                case .result(let result):
                    ResultCard(result: result)
                    Button("Сбросить", role: .destructive) { viewModel.reset() }

                case .error(let error):
                    ErrorView(error: error) { Task { await viewModel.analyze() } }
                        .frame(height: 160)
                }

                Spacer()
            }
            .padding()
            .navigationTitle("Определить")
        }
    }
}

private struct ResultCard: View {
    let result: AnalyzeResponse
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Image(systemName: "leaf.fill").foregroundColor(.green)
                Text(result.displayName).font(.headline)
            }
            if let latin = result.latinName ?? result.primaryResult?.name {
                Text(latin).font(.subheadline).foregroundColor(.secondary)
            }
            if let confidence = result.confidence {
                HStack {
                    Text("Уверенность:").foregroundColor(.secondary)
                    Text("\(Int(confidence * 100))%").bold()
                }
            }
            if let desc = result.description ?? result.primaryResult?.facts {
                Text(desc).font(.caption).foregroundColor(.secondary).lineLimit(3)
            }
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .cornerRadius(12)
    }
}
