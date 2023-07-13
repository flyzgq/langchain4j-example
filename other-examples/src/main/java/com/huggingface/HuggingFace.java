package com.huggingface;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;


@Getter
@Builder
class HuggingFace {

    private String accessToken;
    private String modelId;
    private Duration timeout;
    private Double temperature;
    private Integer maxNewTokens;
    private Boolean returnFullText;
    private Boolean waitForModel;
}
