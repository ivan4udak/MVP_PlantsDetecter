// === File: Features/Identify/IdentifyViewModel.swift ===

import Foundation
import Combine
import UIKit

@MainActor
final class IdentifyViewModel: ObservableObject {

    enum IdentifyState {
        case idle, loading
        case result(AnalyzeResponse)
        case error(AppError)
    }

    @Published private(set) var state: IdentifyState = .idle
    @Published var selectedImage: UIImage?

    private let apiClient: any APIClientProtocol
    init(apiClient: any APIClientProtocol) { self.apiClient = apiClient }

    func analyze() async {
        guard let image = selectedImage,
              let data = image.jpegData(compressionQuality: 0.8) else {
            state = .error(.custom("Выберите фото растения")); return
        }
        state = .loading
        do {
            // TODO: уточнить финальный контракт — multipart vs JSON imageUrl
            let r = try await apiClient.analyzePlant(
                mode: .multipart(imageData: data, mimeType: "image/jpeg", lat: nil, lng: nil))
            state = .result(r)
        } catch let e as AppError { state = .error(e)
        } catch { state = .error(.unknown(error)) }
    }

    func reset() { state = .idle; selectedImage = nil }
}
