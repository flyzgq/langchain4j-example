package com.huggingface.huggingface;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;

import java.time.Duration;

public class ChatModelFactory {

    public static ChatLanguageModel buildHuggingFaceChatModel(){
        HuggingFace huggingFace = buildHuggingFace();
        return   HuggingFaceChatModel.builder()
                .accessToken(huggingFace.getAccessToken())
                .modelId(huggingFace.getModelId())
                .timeout(huggingFace.getTimeout())
                .temperature(huggingFace.getTemperature())
                .maxNewTokens(huggingFace.getMaxNewTokens())
                .returnFullText(huggingFace.getReturnFullText())
                .waitForModel(huggingFace.getWaitForModel())
                .build();
    }

    public static EmbeddingModel buildEmbeddingModel() {
        HuggingFace huggingFace = buildHuggingFace();
        return HuggingFaceEmbeddingModel.builder()
                .accessToken(huggingFace.getAccessToken())
                .modelId(huggingFace.getModelId())
                .waitForModel(huggingFace.getWaitForModel())
                .timeout(huggingFace.getTimeout())
                .build();
    }
    private static HuggingFace buildHuggingFace() {
        return HuggingFace.builder()
                .accessToken("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")
                .modelId("tiiuae/falcon-7b-instruct")
                .timeout(Duration.ofSeconds(1000))
                .temperature(0.1)
                .maxNewTokens(1000)
                .returnFullText(false)
                .waitForModel(true)
                .build();
    }
}
