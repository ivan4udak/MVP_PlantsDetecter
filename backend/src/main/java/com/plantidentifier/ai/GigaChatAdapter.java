package com.plantidentifier.ai;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public class GigaChatAdapter implements AIAdapter {
    @Override
    public AIAnalysisResult analyzeImage(String imageUrl, String language) {
        throw new UnsupportedOperationException("GigaChat not implemented yet");
    }
    @Override public String getProviderName() { return "gigachat"; }
    @Override public String getModelName()    { return "gigachat-pro"; }
    @Override public boolean isAvailable()    { return false; }
}