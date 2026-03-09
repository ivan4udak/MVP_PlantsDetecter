package com.plantidentifier.ai;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YandexAdapter implements AIAdapter {
    @Override
    public AIAnalysisResult analyzeImage(String imageUrl, String language) {
        throw new UnsupportedOperationException("Yandex not implemented yet");
    }
    @Override public String getProviderName() { return "yandex"; }
    @Override public String getModelName()    { return "yandexgpt"; }
    @Override public boolean isAvailable()    { return false; }
}