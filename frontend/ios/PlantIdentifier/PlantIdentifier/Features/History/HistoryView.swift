// === File: Features/History/HistoryView.swift ===

import SwiftUI

struct HistoryView: View {
    @StateObject private var viewModel: HistoryViewModel
    private let apiClient: any APIClientProtocol

    init(apiClient: any APIClientProtocol) {
        self.apiClient = apiClient
        _viewModel = StateObject(wrappedValue: HistoryViewModel(apiClient: apiClient))
    }

    var body: some View {
        NavigationStack {
            Group {
                switch viewModel.state {
                case .idle, .loading:
                    LoadingView(message: "Загружаем историю...")

                case .loaded(let items) where items.isEmpty:
                    EmptyStateView(icon: "clock", message: "История запросов пуста")

                case .loaded(let items):
                    List {
                        ForEach(items) { item in
                            NavigationLink(destination: PlantDetailView(id: item.requestId, apiClient: apiClient)) {
                                HistoryRow(item: item)
                            }
                        }
                        .onDelete { indexSet in
                            for index in indexSet {
                                let id = items[index].requestId
                                Task { await viewModel.delete(id: id) }
                            }
                        }
                    }

                case .error(let error):
                    ErrorView(error: error) {
                        Task { await viewModel.loadHistory() }
                    }
                }
            }
            .navigationTitle("История")
            .refreshable { await viewModel.loadHistory() }
        }
        .task { await viewModel.loadHistory() }
    }
}

private struct HistoryRow: View {
    let item: PlantHistoryItem

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(item.displayName).font(.headline)
            HStack {
                if let conf = item.confidence {
                    Text("\(Int(conf * 100))%").font(.caption).foregroundColor(.secondary)
                }
                if let date = item.createdDate {
                    Text(date).font(.caption).foregroundColor(.secondary)
                }
            }
        }
        .padding(.vertical, 4)
    }
}

struct PlantDetailView: View {
    let id: String
    let apiClient: any APIClientProtocol

    @State private var result: AnalyzeResponse?
    @State private var error: AppError?
    @State private var isLoading = true

    var body: some View {
        Group {
            if isLoading {
                LoadingView()
            } else if let error {
                ErrorView(error: error, retry: nil)
            } else if let result {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        Text(result.displayName).font(.title2.bold())
                        if let conf = result.confidence {
                            Text("Уверенность: \(Int(conf * 100))%").foregroundColor(.secondary)
                        }
                        if let desc = result.description ?? result.primaryResult?.facts {
                            Text(desc)
                        }
                    }
                    .padding()
                }
            }
        }
        .navigationTitle("Детали")
        .task {
            do {
                result = try await apiClient.fetchPlant(id: id)
            } catch let e as AppError {
                error = e
            } catch {
                self.error = .unknown(error)
            }
            isLoading = false
        }
    }
}
