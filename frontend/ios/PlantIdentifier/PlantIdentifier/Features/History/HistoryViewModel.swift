// === File: Features/History/HistoryViewModel.swift ===

import Foundation
import Combine

@MainActor
final class HistoryViewModel: ObservableObject {

    enum HistoryState {
        case idle, loading
        case loaded([PlantHistoryItem])
        case error(AppError)
    }

    @Published private(set) var state: HistoryState = .idle

    private let apiClient: any APIClientProtocol
    init(apiClient: any APIClientProtocol) { self.apiClient = apiClient }

    func loadHistory() async {
        state = .loading
        do {
            let items = try await apiClient.fetchHistory()
            state = .loaded(items)
        } catch let e as AppError { state = .error(e)
        } catch { state = .error(.unknown(error)) }
    }

    func delete(id: String) async {
        do {
            try await apiClient.deletePlant(id: id)
            await loadHistory()
        } catch let e as AppError { state = .error(e)
        } catch { state = .error(.unknown(error)) }
    }
}
